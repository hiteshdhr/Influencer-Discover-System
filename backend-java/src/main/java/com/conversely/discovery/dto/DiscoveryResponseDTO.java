package com.conversely.discovery.dto;

import com.conversely.discovery.model.Creator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Outbound response DTO — returned by POST /api/discover
 *
 * Example:
 * {
 *   "total": 5,
 *   "creators": [ { "name": "...", "fitScore": 82, ... } ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryResponseDTO {

    private int total;
    private String query;
    
    private List<Creator> creators;
}
