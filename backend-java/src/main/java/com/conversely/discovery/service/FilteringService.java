package com.conversely.discovery.service;

import com.conversely.discovery.model.Creator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FilteringService — narrows the raw YouTube result set to genuine micro-influencers.
 *
 * Rules applied:
 *   1. Subscriber count between min (5 000) and max (100 000)
 *   2. Indian creator heuristic (country tag OR name/description signals)
 */
@Slf4j
@Service
public class FilteringService {

    @Value("${filter.subscribers.min}")
    private long minSubscribers;

    @Value("${filter.subscribers.max}")
    private long maxSubscribers;

    // Keywords that suggest an Indian creator when country tag is absent
    private static final List<String> INDIA_SIGNALS = List.of(
            "india", "hindi", "indian", "bharat", "desi",
            "mumbai", "delhi", "bangalore", "hyderabad", "chennai",
            "kolkata", "pune", "rupee", "₹", "iit", "nit", "cbse"
    );

    /**
     * Filter creators to micro-influencer range (5K–100K subs).
     */
    public List<Creator> filterMicroInfluencers(List<Creator> creators) {
        List<Creator> filtered = creators.stream()
                .filter(c -> c.getFollowers() >= minSubscribers && c.getFollowers() <= maxSubscribers)
                .collect(Collectors.toList());

        log.debug("Filtering: {} total → {} micro-influencers ({}K–{}K subs)",
                creators.size(), filtered.size(),
                minSubscribers / 1000, maxSubscribers / 1000);

        return filtered;
    }

    /**
     * Filter to Indian creators only (optional — use when targeting India).
     * Checks the country field first, then falls back to text heuristics.
     */
    public List<Creator> filterIndianCreators(List<Creator> creators) {
        return creators.stream()
                .filter(this::isIndianCreator)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private boolean isIndianCreator(Creator creator) {
        // 1. Explicit country tag from YouTube
        if ("India".equalsIgnoreCase(creator.getRegion())) {
            return true;
        }

        // 2. Heuristic — scan name + description for India signals
        String combined = (
                safeStr(creator.getName()) + " " + safeStr(creator.getDescription())
        ).toLowerCase();

        return INDIA_SIGNALS.stream().anyMatch(combined::contains);
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }
}
