package com.conversely.discovery.service;

import com.conversely.discovery.model.Creator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FitScoringService — computes a brand-creator fit score (0–100).
 *
 * Scoring breakdown (100 pts total):
 *   ┌────────────────────────────────────┬───────┐
 *   │ Factor                             │ Max   │
 *   ├────────────────────────────────────┼───────┤
 *   │ Keyword overlap (themes + niche)   │  40   │
 *   │ Subscriber sweet-spot (20K–60K)    │  20   │
 *   │ Estimated engagement rate          │  20   │
 *   │ Brand-context text overlap         │  20   │
 *   └────────────────────────────────────┴───────┘
 */
@Slf4j
@Service
public class FitScoringService {

    /**
     * Calculates and sets the fitScore on the creator object.
     *
     * @param creator      Enriched creator (must have niche, themes, engagementRate set)
     * @param keywords     Search keywords from the request
     * @param brandContext Brand description from the request
     */
    public void score(Creator creator, List<String> keywords, String brandContext) {
        int keywordScore    = scoreKeywordOverlap(creator, keywords);
        int subscriberScore = scoreSubscriberRange(creator.getFollowers());
        int engagementScore = scoreEngagement(creator.getEngagementRate());
        int brandScore      = scoreBrandContext(creator, brandContext);

        int total = keywordScore + subscriberScore + engagementScore + brandScore;
        total = Math.min(100, Math.max(0, total)); // clamp

        creator.setFitScore(total);

        log.debug("Creator '{}' scores — keyword:{} subscriber:{} engagement:{} brand:{} = {}",
                creator.getName(), keywordScore, subscriberScore, engagementScore, brandScore, total);
    }

    // ------------------------------------------------------------------
    // Scoring components
    // ------------------------------------------------------------------

    /**
     * How many search keywords appear in the creator's themes + niche? (max 40)
     */
    private int scoreKeywordOverlap(Creator creator, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return 0;

        String creatorText = buildCreatorTextCorpus(creator).toLowerCase();
        long matchCount = keywords.stream()
                .filter(kw -> creatorText.contains(kw.toLowerCase()))
                .count();

        // Scale: all keywords matched → 40 pts
        return (int) Math.min(40, (matchCount * 40.0 / keywords.size()));
    }

    /**
     * Reward the subscriber "sweet spot" of 20K–60K (highest authenticity). (max 20)
     */
    private int scoreSubscriberRange(long followers) {
        if (followers >= 20_000 && followers <= 60_000) return 20;   // Sweet spot
        if (followers >= 10_000 && followers <= 80_000) return 15;   // Good range
        if (followers >= 5_000  && followers <= 100_000) return 10;  // Acceptable
        return 5; // Edge of range (already filtered but double-check)
    }

    /**
     * Higher engagement = better reach quality. (max 20)
     *
     *  ≥ 7%  → 20 pts
     *  5–7%  → 16 pts
     *  3–5%  → 12 pts
     *  1–3%  →  8 pts
     *  < 1%  →  4 pts
     */
    private int scoreEngagement(double engagementRate) {
        if (engagementRate >= 7.0) return 20;
        if (engagementRate >= 5.0) return 16;
        if (engagementRate >= 3.0) return 12;
        if (engagementRate >= 1.0) return  8;
        return 4;
    }

    /**
     * Overlap between brand context words and creator's text corpus. (max 20)
     */
    private int scoreBrandContext(Creator creator, String brandContext) {
        if (brandContext == null || brandContext.isBlank()) return 0;

        // Tokenize brand context into meaningful words (3+ chars, no stop words)
        List<String> brandWords = Arrays.stream(brandContext.toLowerCase().split("[^a-z0-9]+"))
                .filter(w -> w.length() >= 3)
                .distinct()
                .collect(Collectors.toList());

        if (brandWords.isEmpty()) return 0;

        String creatorText = buildCreatorTextCorpus(creator).toLowerCase();
        long matched = brandWords.stream()
                .filter(creatorText::contains)
                .count();

        // Scale to max 20 — cap at 40% match rate for full marks
        double ratio = (double) matched / brandWords.size();
        return (int) Math.min(20, ratio * 50); // 40% → 20 pts
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String buildCreatorTextCorpus(Creator creator) {
        StringBuilder sb = new StringBuilder();
        sb.append(safeStr(creator.getName())).append(" ");
        sb.append(safeStr(creator.getNiche())).append(" ");
        sb.append(safeStr(creator.getDescription())).append(" ");

        if (creator.getRecentThemes() != null) {
            sb.append(String.join(" ", creator.getRecentThemes()));
        }

        if (creator.getRecentVideos() != null) {
            creator.getRecentVideos().forEach(v -> {
                sb.append(" ").append(safeStr(v.getTitle()));
                sb.append(" ").append(safeStr(v.getDescription()));
            });
        }

        return sb.toString();
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }
}
