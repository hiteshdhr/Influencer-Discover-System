package com.conversely.discovery.service;

import com.conversely.discovery.model.Creator;
import com.conversely.discovery.model.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ContentAnalysisService — performs rule-based NLP on video titles and descriptions.
 *
 * Responsibilities:
 *   - Extract recurring themes from video text
 *   - Detect a dominant niche label
 *   - Estimate engagement rate from subscriber count
 */
@Slf4j
@Service
public class ContentAnalysisService {

    // Niche buckets with representative keywords (order = priority)
    private static final Map<String, List<String>> NICHE_KEYWORDS = new LinkedHashMap<>() {{
        put("Frontend Tutorials",    List.of("react", "vue", "angular", "html", "css", "javascript", "js", "frontend", "web dev", "nextjs", "tailwind"));
        put("Full-Stack Dev",        List.of("fullstack", "full stack", "node", "express", "django", "spring", "backend", "api", "rest", "graphql"));
        put("Mobile Development",    List.of("flutter", "android", "ios", "kotlin", "swift", "mobile", "app dev", "react native"));
        put("AI & Machine Learning", List.of("machine learning", "deep learning", "ai", "chatgpt", "llm", "tensorflow", "pytorch", "nlp", "data science"));
        put("Data & Analytics",      List.of("python", "pandas", "sql", "tableau", "powerbi", "data analyst", "excel", "statistics"));
        put("Cloud & DevOps",        List.of("aws", "azure", "gcp", "docker", "kubernetes", "ci/cd", "devops", "terraform", "linux"));
        put("Competitive Coding",    List.of("leetcode", "dsa", "algorithms", "coding interview", "competitive", "cp", "codeforces", "placement"));
        put("Career & Education",    List.of("internship", "job", "resume", "campus", "career", "college", "student", "placement", "edtech", "course"));
        put("System Design",         List.of("system design", "scalability", "microservices", "architecture", "distributed"));
        put("Tech Reviews",          List.of("review", "unboxing", "gadget", "laptop", "phone", "tech news", "product"));
    }};

    // Common English stop-words to skip during theme extraction
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "in", "on", "at", "to", "for", "of",
            "and", "or", "but", "with", "this", "that", "my", "your", "i",
            "we", "you", "it", "be", "are", "was", "were", "has", "have",
            "do", "how", "what", "when", "why", "from", "by", "as", "not",
            "will", "can", "if", "so", "just", "also", "about", "more"
    );

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Enriches a Creator with niche, recentThemes, and engagementRate
     * derived from its recent videos.
     */
    public void analyzeCreator(Creator creator, List<String> searchKeywords) {
        List<Video> videos = creator.getRecentVideos();
        if (videos == null || videos.isEmpty()) {
            creator.setNiche("General Tech");
            creator.setRecentThemes(new ArrayList<>(searchKeywords));
            creator.setEngagementRate(estimateEngagementRate(creator.getFollowers()));
            return;
        }

        List<String> themes = extractThemes(videos, searchKeywords);
        String niche = detectNiche(videos, creator.getDescription());

        creator.setNiche(niche);
        creator.setRecentThemes(themes);
        creator.setEngagementRate(estimateEngagementRate(creator.getFollowers()));

        log.debug("Creator '{}' → niche: {}, themes: {}", creator.getName(), niche, themes);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Extracts top-N meaningful keywords from all video titles and descriptions.
     */
    private List<String> extractThemes(List<Video> videos, List<String> searchKeywords) {
        // Build a word-frequency map across all video text
        Map<String, Integer> freq = new HashMap<>();

        for (Video v : videos) {
            String text = (safeStr(v.getTitle()) + " " + safeStr(v.getDescription())).toLowerCase();
            String[] words = text.split("[^a-z0-9#.+]+");
            for (String word : words) {
                if (word.length() < 3) continue;
                if (STOP_WORDS.contains(word)) continue;
                freq.merge(word, 1, Integer::sum);
            }
        }

        // Boost search keywords that appear in the text
        for (String kw : searchKeywords) {
            String lower = kw.toLowerCase();
            if (freq.containsKey(lower)) {
                freq.put(lower, freq.get(lower) + 5);
            }
        }

        // Return top 6 by frequency, formatted as Title Case
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(e -> toTitleCase(e.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Detects the dominant niche by scoring each niche bucket against video text + description.
     */
    private String detectNiche(List<Video> videos, String channelDescription) {
        String corpus = videos.stream()
                .map(v -> safeStr(v.getTitle()) + " " + safeStr(v.getDescription()))
                .collect(Collectors.joining(" "))
                .toLowerCase();

        corpus += " " + safeStr(channelDescription).toLowerCase();

        String bestNiche = "General Tech";
        int bestScore = 0;

        for (Map.Entry<String, List<String>> entry : NICHE_KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue()) {
                if (corpus.contains(kw)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestNiche = entry.getKey();
            }
        }

        return bestNiche;
    }

    /**
     * Estimates engagement rate using a research-backed inverse-log formula.
     * Micro-influencers (10K–50K) typically get 3–7%.
     */
    private double estimateEngagementRate(long followers) {
        if (followers <= 0) return 2.0;
        // Empirical model: smaller channels engage better
        double rate = 8.0 - (Math.log10(followers) - 3) * 1.5;
        rate = Math.max(1.5, Math.min(rate, 9.0));
        return Math.round(rate * 10.0) / 10.0; // 1 decimal place
    }

    private String toTitleCase(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }
}
