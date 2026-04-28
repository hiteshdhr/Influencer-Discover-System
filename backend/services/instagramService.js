const { ApifyClient } = require('apify-client');

/**
 * Searches Instagram using the Apify "instagram-scraper" or "instagram-hashtag-scraper".
 * This requires an APIFY_API_TOKEN in the .env file.
 */
exports.searchAndFilterCreators = async (keywords) => {
    if (!process.env.APIFY_API_TOKEN || process.env.APIFY_API_TOKEN === 'YOUR_APIFY_TOKEN_HERE') {
        console.warn('[Instagram Service] APIFY_API_TOKEN is missing. Skipping Instagram discovery.');
        return [];
    }

    const client = new ApifyClient({
        token: process.env.APIFY_API_TOKEN,
    });

    const primaryHashtag = keywords[0].replace(/\s+/g, '');
    console.log(`[Instagram Service] Triggering Apify actor for hashtag: #${primaryHashtag}`);

    try {
        // MOCK APIFY FOR NOW TO PREVENT RATE LIMITING BACKOFF HANGING THE SERVER
        console.log(`[Instagram Service] Bypassing Apify Actor due to rate limits and returning dummy Instagram data.`);
        const validCreators = [
            {
                creator_id: `ig_12345`,
                platform: "Instagram",
                username: "react_master",
                profile_url: `https://instagram.com/react_master`,
                metrics: { followers: 15000, engagement_rate: 4.2, average_views: 4500 },
                demographics: { region: "India", language: "English" },
                content_intelligence: {
                    primary_niche: keywords[0],
                    sub_segments: keywords.slice(1),
                    recent_themes: ["React tips", "Web dev"],
                    recent_post_url: `https://instagram.com/p/mock123`,
                    recent_post_title: `Reel about ${keywords[0]}`
                },
                contact: { email: "react@example.com", instagram_handle: `@react_master` }
            }
        ];

        console.log(`[Instagram Service] Discovered ${validCreators.length} valid profiles.`);
        return validCreators;

    } catch (error) {
        console.error('[Instagram Service] Apify Error:', error);
        return [];
    }
};
