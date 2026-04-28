/**
 * Calculates a Brand-Creator fit score based on keyword overlap
 * between the brand context and the creator's recent themes.
 * 
 * In a production app, this would use an LLM or vector DB (like Pinecone) 
 * with HuggingFace embeddings for true semantic similarity.
 */
exports.calculateFitScore = (brandContext, creatorThemes, engagementRate) => {
    const brandTokens = brandContext.toLowerCase().split(/[\s,.-]+/);
    const themeString = creatorThemes.join(" ").toLowerCase();
    
    let matchCount = 0;
    for (const token of brandTokens) {
        if (token.length > 3 && themeString.includes(token)) {
            matchCount++;
        }
    }

    // Heuristic scoring for prototype
    // Base score from matches (cap at 80)
    const maxTokensToCareAbout = 2;
    const matchRatio = Math.min(matchCount / maxTokensToCareAbout, 1);
    const relevanceScore = matchRatio * 80;

    // Engagement Weighting
    // e.g. 5% ER is 1.0 multiplier. 8% ER is 1.6
    const engagementMultiplier = Math.min(engagementRate / 5.0, 1.2);

    let finalScore = relevanceScore * engagementMultiplier;

    // Add a base floor if no direct matches but engagement is good just to see the data (optional)
    if (finalScore === 0) finalScore = 15; 

    return Math.min(Math.floor(finalScore), 100);
};
