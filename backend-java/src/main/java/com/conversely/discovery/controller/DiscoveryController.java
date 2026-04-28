package com.conversely.discovery.controller;

import com.conversely.discovery.dto.DiscoveryRequestDTO;
import com.conversely.discovery.dto.DiscoveryResponseDTO;
import com.conversely.discovery.service.DiscoveryOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DiscoveryController — REST entry point for the Micro-Influencer Discovery API.
 *
 * Endpoints:
 *   POST /api/discover   → Run the full discovery pipeline
 *   GET  /api/health     → Simple health check
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryOrchestrationService orchestrationService;

    /**
     * POST /api/discover
     *
     * Request body:
     * {
     *   "keywords": ["react", "frontend"],
     *   "brandContext": "Ed-tech platform helping students..."
     * }
     *
     * Response:
     * {
     *   "total": 7,
     *   "query": "react, frontend",
     *   "creators": [
     *     {
     *       "name": "CodeWithHarry",
     *       "platform": "YouTube",
     *       "followers": 45000,
     *       "niche": "Frontend Tutorials",
     *       "fitScore": 87,
     *       "outreach": {
     *         "emailSubject": "...",
     *         "emailBody": "...",
     *         "dmText": "..."
     *       }
     *     }
     *   ]
     * }
     */
    @PostMapping("/discover")
    public ResponseEntity<DiscoveryResponseDTO> discover(
            @Valid @RequestBody DiscoveryRequestDTO request) {

        log.info("POST /api/discover — keywords={}", request.getKeywords());

        DiscoveryResponseDTO response = orchestrationService.runDiscovery(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/health
     * Used by the frontend and load-balancers to verify the service is up.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"ok\",\"service\":\"Micro-Influencer Discovery API (Java)\"}");
    }
}
