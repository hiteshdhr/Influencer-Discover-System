const youtubeService = require('../services/youtubeService');
const instagramService = require('../services/instagramService');
const nlpScoringService = require('../services/nlpScoringService');
const outreachService = require('../services/outreachService');

exports.runDiscovery = async (req, res) => {
    try {
        const { keywords, brandContext } = req.body;

        if (!keywords || !Array.isArray(keywords) || !brandContext) {
            return res.status(400).json({ error: 'Missing or invalid keywords array or brandContext string' });
        }

        console.log(`[Discovery] Starting for keywords: ${keywords.join(', ')}`);

        // 1. Discovery & Filtering (YouTube + Instagram in parallel)
        const [youtubeProfiles, instagramProfiles] = await Promise.all([
            youtubeService.searchAndFilterCreators(keywords),
            instagramService.searchAndFilterCreators(keywords)
        ]);

        const rawProfiles = [...youtubeProfiles, ...instagramProfiles];
        console.log(`[Discovery] Found ${rawProfiles.length} valid profiles across platforms.`);

        const processedCreators = [];

        for (const profile of rawProfiles) {
            // 2. Content Intelligence & Scoring
            const fitScore = nlpScoringService.calculateFitScore(brandContext, profile.content_intelligence.recent_themes, profile.metrics.engagement_rate);
            
            // Assign score
            profile.fit_score = fitScore;

            // 3. Outreach Generation (Only for high scores, > 75)
            if (fitScore >= 75) {
                const draftOutreach = outreachService.generateOutreach(profile, brandContext);
                profile.outreach_drafts = draftOutreach;
                profile.status = "High Fit - Outreach Ready";
            } else {
                profile.status = "Low Fit - Discarded";
            }

            processedCreators.push(profile);
        }

        // Sort by score descending
        processedCreators.sort((a, b) => b.fit_score - a.fit_score);

        res.json({
            success: true,
            message: 'Discovery complete',
            results: processedCreators
        });

    } catch (error) {
        console.error('[Error] runDiscovery:', error);
        res.status(500).json({ error: 'Internal server error during discovery' });
    }
};
