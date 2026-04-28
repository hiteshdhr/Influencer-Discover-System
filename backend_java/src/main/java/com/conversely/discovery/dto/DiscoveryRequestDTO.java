package com.conversely.discovery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Inbound request DTO — accepted by POST /api/discover
 *
 * Example:
 * {
 *   "keywords": ["react", "frontend"],
 *   "brandContext": "Ed-tech platform helping students..."
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryRequestDTO {

    @NotEmpty(message = "At least one keyword is required")
    private List<String> keywords;

    @NotBlank(message = "Brand context must not be blank")
    private String brandContext;
}
