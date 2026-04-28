package com.conversely.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single YouTube video fetched for content analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    private String videoId;
    private String title;
    private String description;
    private String publishedAt;
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private String thumbnailUrl;
}
