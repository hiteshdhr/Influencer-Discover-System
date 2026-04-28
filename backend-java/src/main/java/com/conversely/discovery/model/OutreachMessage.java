package com.conversely.discovery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds generated outreach copy (email + DM) for a creator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutreachMessage {

    private String emailSubject;
    private String emailBody;   // 60–90 words
    private String dmText;      // 15–30 words
}
