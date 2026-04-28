package com.conversely.discovery.service;

import com.conversely.discovery.exception.YouTubeApiException;
import com.conversely.discovery.model.Creator;
import com.conversely.discovery.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * YouTubeService — integrates with YouTube Data API v3.
 *
 * Endpoints used:
 *   1. GET /search       → find channels by keyword
 *   2. GET /channels     → fetch statistics + snippet for those channels
 *   3. GET /search       → fetch recent videos for a channel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeService {

    private final RestTemplate restTemplate;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.base-url}")
    private String baseUrl;

    @Value("${youtube.search.max-results}")
    private int maxResults;

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Search YouTube for channels matching a keyword.
     * Returns a list of partial Creator objects (only id populated).
     */
    public List<String> searchChannelIdsByKeyword(String keyword) {
        log.debug("Searching YouTube channels for keyword: {}", keyword);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "channel")
                .queryParam("q", keyword)
                .queryParam("maxResults", maxResults)
                .queryParam("relevanceLanguage", "en")
                .queryParam("key", apiKey)
                .build().toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return extractChannelIds(response);
        } catch (RestClientException e) {
            log.error("YouTube search API call failed for keyword '{}': {}", keyword, e.getMessage());
            throw new YouTubeApiException("YouTube search failed: " + e.getMessage());
        }
    }

    /**
     * Fetch detailed channel info (snippet + statistics) for a batch of channel IDs.
     * YouTube allows up to 50 IDs per request.
     */
    public List<Creator> getChannelDetails(List<String> channelIds) {
        if (channelIds.isEmpty()) return Collections.emptyList();

        log.debug("Fetching details for {} channels", channelIds.size());

        List<Creator> allCreators = new ArrayList<>();

        // YouTube API allows max 50 IDs per request
        for (int i = 0; i < channelIds.size(); i += 50) {
            List<String> batch = channelIds.subList(i, Math.min(i + 50, channelIds.size()));
            String ids = String.join(",", batch);

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/channels")
                    .queryParam("part", "snippet,statistics,brandingSettings")
                    .queryParam("id", ids)
                    .queryParam("maxResults", 50)
                    .queryParam("key", apiKey)
                    .build().toUriString();

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                allCreators.addAll(parseChannelItems(response));
            } catch (RestClientException e) {
                log.error("YouTube channels API call failed for batch: {}", e.getMessage());
                throw new YouTubeApiException("YouTube channels fetch failed: " + e.getMessage());
            }
        }
        
        return allCreators;
    }

    /**
     * Fetch the most recent 10 videos for a given channel.
     */
    public List<Video> getRecentVideos(String channelId) {
        log.debug("Fetching recent videos for channel: {}", channelId);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("part", "snippet")
                .queryParam("channelId", channelId)
                .queryParam("type", "video")
                .queryParam("order", "date")
                .queryParam("maxResults", 10)
                .queryParam("key", apiKey)
                .build().toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseVideoItems(response);
        } catch (RestClientException e) {
            log.warn("Could not fetch videos for channel {}: {}", channelId, e.getMessage());
            return Collections.emptyList(); // Non-fatal — proceed without videos
        }
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<String> extractChannelIds(Map<String, Object> response) {
        List<String> ids = new ArrayList<>();
        if (response == null) return ids;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) return ids;

        for (Map<String, Object> item : items) {
            Map<String, Object> idObj = (Map<String, Object>) item.get("id");
            if (idObj != null) {
                String channelId = (String) idObj.get("channelId");
                if (channelId != null) ids.add(channelId);
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private List<Creator> parseChannelItems(Map<String, Object> response) {
        List<Creator> creators = new ArrayList<>();
        if (response == null) return creators;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) return creators;

        for (Map<String, Object> item : items) {
            try {
                String channelId = (String) item.get("id");

                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                Map<String, Object> stats   = (Map<String, Object>) item.get("statistics");
                Map<String, Object> branding = (Map<String, Object>) item.getOrDefault("brandingSettings", Map.of());
                Map<String, Object> brandingChannel = (Map<String, Object>) branding.getOrDefault("channel", Map.of());

                String name        = (String) snippet.getOrDefault("title", "Unknown");
                String description = (String) snippet.getOrDefault("description", "");
                String country     = (String) snippet.getOrDefault("country", "");

                // Thumbnail
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                String thumbnail = extractThumbnail(thumbnails);

                // Statistics (YouTube returns these as strings)
                long subscribers = parseLong(stats, "subscriberCount");
                long totalViews  = parseLong(stats, "viewCount");
                long videoCount  = parseLong(stats, "videoCount");

                Creator creator = Creator.builder()
                        .creatorId(channelId)
                        .name(name)
                        .platform("YouTube")
                        .channelUrl("https://www.youtube.com/channel/" + channelId)
                        .profileImageUrl(thumbnail)
                        .description(description)
                        .followers(subscribers)
                        .totalViews(totalViews)
                        .videoCount(videoCount)
                        .region(resolveRegion(country))
                        .build();

                creators.add(creator);
            } catch (Exception e) {
                log.warn("Failed to parse channel item: {}", e.getMessage());
            }
        }
        return creators;
    }

    @SuppressWarnings("unchecked")
    private List<Video> parseVideoItems(Map<String, Object> response) {
        List<Video> videos = new ArrayList<>();
        if (response == null) return videos;

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items == null) return videos;

        for (Map<String, Object> item : items) {
            try {
                Map<String, Object> idObj    = (Map<String, Object>) item.get("id");
                Map<String, Object> snippet  = (Map<String, Object>) item.get("snippet");

                String videoId    = (String) idObj.getOrDefault("videoId", "");
                String title      = (String) snippet.getOrDefault("title", "");
                String description = (String) snippet.getOrDefault("description", "");
                String publishedAt = (String) snippet.getOrDefault("publishedAt", "");

                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                String thumbnail = extractThumbnail(thumbnails);

                Video video = Video.builder()
                        .videoId(videoId)
                        .title(title)
                        .description(description)
                        .publishedAt(publishedAt)
                        .thumbnailUrl(thumbnail)
                        .build();

                videos.add(video);
            } catch (Exception e) {
                log.warn("Failed to parse video item: {}", e.getMessage());
            }
        }
        return videos;
    }

    @SuppressWarnings("unchecked")
    private String extractThumbnail(Map<String, Object> thumbnails) {
        if (thumbnails == null) return "";
        // Prefer medium > default > high
        for (String size : new String[]{"medium", "default", "high"}) {
            Map<String, Object> t = (Map<String, Object>) thumbnails.get(size);
            if (t != null && t.get("url") != null) {
                return (String) t.get("url");
            }
        }
        return "";
    }

    private long parseLong(Map<String, Object> map, String key) {
        if (map == null) return 0L;
        Object val = map.get(key);
        if (val == null) return 0L;
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String resolveRegion(String country) {
        if (country == null || country.isBlank()) return "Unknown";
        return switch (country.toUpperCase()) {
            case "IN" -> "India";
            case "US" -> "United States";
            case "GB" -> "United Kingdom";
            case "CA" -> "Canada";
            case "AU" -> "Australia";
            default   -> country;
        };
    }
}
