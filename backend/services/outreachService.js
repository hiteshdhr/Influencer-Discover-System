/**
 * Generates contextual outreach payloads.
 * In production, this would call OpenAI API to generate natural language.
 */
exports.generateOutreach = (profile, brandContext) => {
    const recentVideo = profile.content_intelligence.recent_post_title;
    const niche = profile.content_intelligence.primary_niche;
    const firstName = profile.username.replace(/([A-Z])/g, ' $1').trim().split(' ')[0] || "Creator"; // Quick hack for CodeWithAditi -> Code

    const emailBody = `Hi ${firstName},

I loved your recent video on '${recentVideo}'—your insights were incredibly clear! 
I run outreach for our brand, focusing on: ${brandContext}.

Given your focus on ${niche} and your highly engaged audience, I think our new campaign would deeply resonate with them. We'd love to sponsor a dedicated segment in your upcoming video.

Are you open to discussing a collaboration this week?

Best,
[Your Name]
Partner Manager`;

    const dmBody = `Hey ${firstName}! 👋 Absolutely loved your recent content on ${recentVideo}. We're looking for partners for our ${brandContext} campaign and think your audience is a perfect fit. Open to a quick chat about a paid collab? Let me know!`;

    return {
        email: {
            subject: `Loved your recent video! Collab with us?`,
            body: emailBody
        },
        instagram_dm: dmBody
    };
};
