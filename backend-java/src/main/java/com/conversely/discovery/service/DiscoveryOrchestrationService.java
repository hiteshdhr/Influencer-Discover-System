package com.conversely.discovery.service;

import com.conversely.discovery.dto.DiscoveryRequestDTO;
import com.conversely.discovery.dto.DiscoveryResponseDTO;
import com.conversely.discovery.model.Creator;
import com.conversely.discovery.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DiscoveryOrchestrationService — the main pipeline coordinator.
 *
 * Pipeline:
 *   Request
 *     → 1. YouTubeService.searchChannelIds (per keyword)
 *     → 2. YouTubeService.getChannelDetails (deduplicated channel IDs)
 *     → 3. FilteringService.filterMicroInfluencers
 *     → 4. For each creator: YouTubeService.getRecentVideos
 *     → 5. ContentAnalysisService.analyzeCreator
 *     → 6. FitScoringService.score
 *     → 7. OutreachService.generateOutreach
 *     → 8. Sort by fitScore DESC
 *   Response
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoveryOrchestrationService {

    private final YouTubeService        youTubeService;
    private final FilteringService      filteringService;
    private final ContentAnalysisService contentAnalysisService;
    private final FitScoringService     fitScoringService;
    private final OutreachService       outreachService;

    public DiscoveryResponseDTO runDiscovery(DiscoveryRequestDTO request) {
        List<String>  keywords     = request.getKeywords();
        String        brandContext = request.getBrandContext();

        log.info("Starting discovery for keywords={} brandContext='{}'", keywords, brandContext);

        // ── Step 1: Search YouTube for channel IDs across all keywords ────────
        Set<String> allChannelIds = new LinkedHashSet<>();
        for (String keyword : keywords) {
            List<String> ids = youTubeService.searchChannelIdsByKeyword(keyword);
            allChannelIds.addAll(ids);
            log.debug("Keyword '{}' → {} channels", keyword, ids.size());
        }
        log.info("Total unique channels found: {}", allChannelIds.size());

        if (allChannelIds.isEmpty()) {
            return DiscoveryResponseDTO.builder()
                    .total(0)
                    .query(String.join(", ", keywords))
                    .creators(Collections.emptyList())
                    .build();
        }

        // ── Step 2: Fetch full channel details (batch) ────────────────────────
        List<String> idList = new ArrayList<>(allChannelIds);
        List<Creator> rawCreators = youTubeService.getChannelDetails(idList);
        log.info("Fetched details for {} channels", rawCreators.size());

        // ── Step 3: Filter to micro-influencer range (5K–100K subs) ──────────
        List<Creator> filtered = filteringService.filterMicroInfluencers(rawCreators);
        log.info("After filtering: {} micro-influencers", filtered.size());

        if (filtered.isEmpty()) {
            return DiscoveryResponseDTO.builder()
                    .total(0)
                    .query(String.join(", ", keywords))
                    .creators(Collections.emptyList())
                    .build();
        }

        // ── Steps 4–7: Enrich each creator ───────────────────────────────────
        List<Creator> enriched = filtered.stream().map(creator -> {
            try {
                // 4. Fetch recent videos for content analysis
                List<Video> videos = youTubeService.getRecentVideos(creator.getCreatorId());
                creator.setRecentVideos(videos);

                // 5. Analyse content → niche, themes, engagementRate
                contentAnalysisService.analyzeCreator(creator, keywords);

                // 6. Compute fit score
                fitScoringService.score(creator, keywords, brandContext);

                // 7. Generate outreach copy (only for fit score ≥ 50)
                if (creator.getFitScore() >= 50) {
                    outreachService.generateOutreach(creator, brandContext);
                }

                return creator;
            } catch (Exception e) {
                log.warn("Error enriching creator '{}': {}", creator.getName(), e.getMessage());
                return null; // Will be filtered out below
            }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        // ── Step 8: Sort by fitScore descending ──────────────────────────────
        enriched.sort(Comparator.comparingInt(Creator::getFitScore).reversed());

        log.info("Discovery complete. Returning {} enriched creators.", enriched.size());

        return DiscoveryResponseDTO.builder()
                .total(enriched.size())
                .query(String.join(", ", keywords))
                .creators(enriched)
                .build();
    }
}
