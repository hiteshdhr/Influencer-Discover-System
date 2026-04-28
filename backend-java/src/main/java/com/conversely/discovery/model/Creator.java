package com.conversely.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Core domain model representing a discovered micro-influencer creator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Creator {

    private String creatorId;
    private String name;
    private String platform;           // "YouTube" or "Instagram"
    private String channelUrl;
    private String profileImageUrl;
    private String description;

    private long followers;            // subscriber count
    private long totalViews;
    private long videoCount;
    private double engagementRate;     // estimated %

    private String niche;              // e.g. "Frontend Tutorials"
    private String region;             // "India" or detected country
    private List<String> recentThemes;

    private int fitScore;              // 0–100
    private OutreachMessage outreach;
    private List<Video> recentVideos;
}
