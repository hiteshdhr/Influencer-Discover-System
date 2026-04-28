package com.conversely.discovery.service;

import com.conversely.discovery.model.Creator;
import com.conversely.discovery.model.OutreachMessage;
import com.conversely.discovery.model.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OutreachService — generates personalised outreach copy for each creator.
 *
 * Output:
 *   - emailSubject : concise subject line
 *   - emailBody    : 60–90 words, professional but warm
 *   - dmText       : 15–30 words, casual and direct
 *
 * Personalisation signals used:
 *   - Creator name
 *   - Detected niche
 *   - Most recent video title (if available)
 *   - Fit score (high-fit gets a stronger CTA)
 */
@Slf4j
@Service
public class OutreachService {

    private static final int HIGH_FIT_THRESHOLD = 75;

    /**
     * Generates and attaches an OutreachMessage to the creator.
     *
     * @param creator      Enriched creator with niche + fitScore set
     * @param brandContext Brand description from the request
     */
    public void generateOutreach(Creator creator, String brandContext) {
        String recentVideoTitle = extractRecentVideoTitle(creator);
        OutreachMessage msg = OutreachMessage.builder()
                .emailSubject(buildEmailSubject(creator))
                .emailBody(buildEmailBody(creator, brandContext, recentVideoTitle))
                .dmText(buildDmText(creator))
                .build();

        creator.setOutreach(msg);
        log.debug("Generated outreach for '{}'", creator.getName());
    }

    // ------------------------------------------------------------------
    // Email Subject
    // ------------------------------------------------------------------

    private String buildEmailSubject(Creator creator) {
        return String.format("Collaboration Opportunity — %s × Conversely", creator.getName());
    }

    // ------------------------------------------------------------------
    // Email Body (60–90 words)
    // ------------------------------------------------------------------

    private String buildEmailBody(Creator creator, String brandContext, String recentVideoTitle) {
        String niche  = safeStr(creator.getNiche(), "tech content");
        String name   = creator.getName();
        boolean highFit = creator.getFitScore() >= HIGH_FIT_THRESHOLD;

        // Reference a recent video if available for personalisation
        String videoReference = recentVideoTitle.isEmpty()
                ? String.format("your work in the %s space", niche)
                : String.format("your recent video \"%s\"", recentVideoTitle);

        String cta = highFit
                ? "We'd love to explore a paid collaboration — a sponsored video or a dedicated integration would work perfectly for both our audiences."
                : "We think there's a genuine fit here and would love to discuss a potential collaboration that benefits your audience.";

        return String.format(
                "Hi %s,%n%n" +
                "I came across %s and was genuinely impressed — your audience clearly trusts your take on %s." +
                "%n%n" +
                "We're building Conversely, a platform that helps creators and brands discover each other authentically. " +
                "Given your focus on %s, we believe your audience would find real value in what we do." +
                "%n%n" +
                "%s" +
                "%n%n" +
                "Would you be open to a quick 15-minute chat? Happy to share more details.%n%n" +
                "Warm regards,%n" +
                "Team Conversely",
                name, videoReference, niche, niche, cta
        );
    }

    // ------------------------------------------------------------------
    // DM (15–30 words)
    // ------------------------------------------------------------------

    private String buildDmText(Creator creator) {
        String niche = safeStr(creator.getNiche(), "your content");
        boolean highFit = creator.getFitScore() >= HIGH_FIT_THRESHOLD;

        if (highFit) {
            return String.format(
                    "Hey %s! Love your %s content 🔥 We're Conversely — a creator-brand platform. " +
                    "Paid collab opportunity for you. Interested?",
                    creator.getName(), niche
            );
        } else {
            return String.format(
                    "Hey %s! Big fan of your %s videos. " +
                    "Think there's a cool collab opportunity with Conversely — mind if I share details?",
                    creator.getName(), niche
            );
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String extractRecentVideoTitle(Creator creator) {
        List<Video> videos = creator.getRecentVideos();
        if (videos == null || videos.isEmpty()) return "";
        // Take the first (most recent) video title, truncate if too long
        String title = videos.get(0).getTitle();
        if (title == null || title.isBlank()) return "";
        return title.length() > 60 ? title.substring(0, 57) + "..." : title;
    }

    private String safeStr(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
