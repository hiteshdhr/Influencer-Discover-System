const { google } = require('googleapis');

// Initialize the YouTube client
// Make sure YOUTUBE_API_KEY is set in your .env file
const getYouTubeClient = () => {
    return google.youtube({
        version: 'v3',
        auth: process.env.YOUTUBE_API_KEY
    });
};

/**
 * Searches YouTube for creators based on keywords, 
 * fetching real videos and filtering by follower counts (5k-100k).
 */
exports.searchAndFilterCreators = async (keywords) => {
    if (!process.env.YOUTUBE_API_KEY || process.env.YOUTUBE_API_KEY === 'YOUR_API_KEY_HERE') {
        throw new Error('YOUTUBE_API_KEY is missing or invalid in .env file.');
    }

    const youtube = getYouTubeClient();
    const query = keywords.join(" ");
    
    console.log(`[YouTube Service] Searching API for: "${query}"`);

    // 1. Search for recent videos matching the keywords
    const searchResponse = await youtube.search.list({
        part: 'snippet',
        q: query,
        type: 'video',
        maxResults: 15,
        order: 'relevance',
        regionCode: 'IN' // Bias towards Indian creators
    });

    if (!searchResponse.data.items || searchResponse.data.items.length === 0) {
        return [];
    }

    // Collect unique channel IDs and their recent videos
    const channelVideoMap = {};
    for (const item of searchResponse.data.items) {
        const channelId = item.snippet.channelId;
        if (!channelVideoMap[channelId]) {
            channelVideoMap[channelId] = item;
        }
    }

    const uniqueChannelIds = Object.keys(channelVideoMap);
    console.log(`[YouTube Service] Found ${uniqueChannelIds.length} unique channels. Fetching stats...`);

    // 2. Fetch full channel statistics for these creators
    const channelsResponse = await youtube.channels.list({
        part: 'snippet,statistics',
        id: uniqueChannelIds.join(','),
        maxResults: 50
    });

    const validCreators = [];

    // 3. Filter and map to our system's schema
    for (const channel of channelsResponse.data.items) {
        const stats = channel.statistics;
        const subCount = parseInt(stats.subscriberCount) || 0;
        const viewCount = parseInt(stats.viewCount) || 0;
        const videoCount = parseInt(stats.videoCount) || 1;

        // FILTER: 5k to 100k subscribers
        if (subCount >= 5000 && subCount <= 100000) {
            
            // Rough engagement estimation for prototype 
            // Average views per video relative to subscribers
            const avgViews = viewCount / videoCount;
            let engagementRate = ((avgViews / subCount) * 100).toFixed(1);
            if (engagementRate > 15) engagementRate = 15; // Cap anomalous engagement

            const recentVideo = channelVideoMap[channel.id];
            
            // Attempt to extract an email from channel description (basic regex)
            const desc = channel.snippet.description || "";
            const emailMatch = desc.match(/[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/);
            const extractedEmail = emailMatch ? emailMatch[0] : "Not publicly listed";

            const creatorProfile = {
                creator_id: `yt_${channel.id}`,
                platform: "YouTube",
                username: channel.snippet.title,
                profile_url: `https://youtube.com/channel/${channel.id}`,
                metrics: {
                    followers: subCount,
                    engagement_rate: parseFloat(engagementRate),
                    average_views: Math.floor(avgViews)
                },
                demographics: {
                    region: channel.snippet.country || "India", 
                    language: "Varies"
                },
                content_intelligence: {
                    primary_niche: keywords[0], // approximate based on top keyword
                    sub_segments: keywords.slice(1),
                    recent_themes: [recentVideo.snippet.title, desc.substring(0, 50)],
                    recent_post_url: `https://youtube.com/watch?v=${recentVideo.id.videoId}`,
                    recent_post_title: recentVideo.snippet.title
                },
                contact: {
                    email: extractedEmail,
                    instagram_handle: "Unknown"
                }
            };
            
            validCreators.push(creatorProfile);
        }
    }

    return validCreators;
};
