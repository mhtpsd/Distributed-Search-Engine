package com.mhtpsd.searchengine.ranking;

import com.mhtpsd.searchengine.common.Page;
import com.mhtpsd.searchengine.common.PageRepository;
import com.mhtpsd.searchengine.indexer.IndexerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RankingService combines TF‑IDF scores with title weighting and a simple PageRank score.
 *
 * - Title weighting: terms that appear in the title receive a configurable boost.
 * - PageRank: a lightweight iterative algorithm over the link graph extracted from stored pages.
 */
@Service
public class RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingService.class);

    private final IndexerService indexerService;
    private final PageRepository pageRepository;

    // Boost factor for title terms (e.g., 2.0 means title terms count double)
    private static final double TITLE_BOOST = 2.0;
    // Damping factor for PageRank
    private static final double DAMPING = 0.85;
    // Number of iterations for PageRank computation
    private static final int PR_ITERATIONS = 10;

    public RankingService(IndexerService indexerService, PageRepository pageRepository) {
        this.indexerService = indexerService;
        this.pageRepository = pageRepository;
    }

    /**
     * Compute a final ranking list of Page objects for the given query.
     * The ranking combines:
     *   1) TF‑IDF score (from IndexerService)
     *   2) Title boost (terms in the title are weighted higher)
     *   3) PageRank score (pre‑computed or computed on‑the‑fly)
     *
     * @param query the user query string
     * @param topK maximum number of results to return
     * @return ordered list of Pages (best first)
     */
    public List<Page> rank(String query, int topK) {
        // 1) Get TF‑IDF based page IDs
        List<Long> tfidfIds = indexerService.search(query, topK * 5); // fetch a superset
        if (tfidfIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 2) Retrieve Page entities
        List<Page> candidatePages = indexerService.getPagesByIds(tfidfIds);
        // 3) Compute PageRank scores for all pages (cached in a map)
        Map<Long, Double> pageRankMap = computePageRankScores();
        // 4) Compute final combined score per page
        Map<Long, Double> finalScores = new HashMap<>();
        Set<String> queryTokens = com.mhtpsd.searchengine.util.TokenizerUtil.tokenize(query);
        for (Page page : candidatePages) {
            double tfidfScore = computeTfIdfForPage(page, queryTokens);
            double titleScore = computeTitleBoost(page, queryTokens);
            double prScore = pageRankMap.getOrDefault(page.getId(), 0.0);
            double combined = tfidfScore + titleScore + prScore;
            finalScores.put(page.getId(), combined);
        }
        // 5) Sort pages by combined score descending and limit to topK
        return finalScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(topK)
                .map(e -> candidatePages.stream()
                        .filter(p -> p.getId().equals(e.getKey()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Compute TF‑IDF score for a single page given the query tokens. */
    private double computeTfIdfForPage(Page page, Set<String> queryTokens) {
        double score = 0.0;
        int totalDocs = indexerService.getInvertedIndex().getTotalDocuments();
        for (String token : queryTokens) {
            int tf = indexerService.getInvertedIndex().getTermFrequencies(token).getOrDefault(page.getId(), 0);
            int df = indexerService.getInvertedIndex().getDocumentFrequency(token);
            double idf = df == 0 ? 0.0 : Math.log((double) totalDocs / (double) df);
            score += tf * idf;
        }
        return score;
    }

    /** Boost score for tokens appearing in the title. */
    private double computeTitleBoost(Page page, Set<String> queryTokens) {
        Set<String> titleTokens = com.mhtpsd.searchengine.util.TokenizerUtil.tokenize(page.getTitle());
        long intersect = queryTokens.stream().filter(titleTokens::contains).count();
        return intersect * TITLE_BOOST;
    }

    /**
     * Compute PageRank scores for all pages stored in the repository.
     * This is a simple implementation that parses outbound links from the stored content.
     * Links are stored after a "---LINKS---" delimiter in the content field.
     */
    private Map<Long, Double> computePageRankScores() {
        List<Page> allPages = pageRepository.findAll();
        int n = allPages.size();
        if (n == 0) {
            return Collections.emptyMap();
        }
        // Build adjacency list: pageId -> set of outbound pageIds (by URL lookup)
        Map<Long, Set<Long>> outLinks = new HashMap<>();
        Map<String, Long> urlToId = new HashMap<>();
        for (Page p : allPages) {
            urlToId.put(p.getUrl(), p.getId());
        }
        for (Page p : allPages) {
            Set<Long> targets = new HashSet<>();
            String content = p.getContent();
            if (content != null) {
                int delimIdx = content.indexOf("---LINKS---");
                if (delimIdx != -1) {
                    String linksPart = content.substring(delimIdx + "---LINKS---".length()).trim();
                    for (String link : linksPart.split("\\n")) {
                        link = link.trim();
                        if (!link.isEmpty() && urlToId.containsKey(link)) {
                            targets.add(urlToId.get(link));
                        }
                    }
                }
            }
            outLinks.put(p.getId(), targets);
        }
        // Initialize PageRank values uniformly
        Map<Long, Double> pr = new HashMap<>();
        double initRank = 1.0 / n;
        for (Page p : allPages) {
            pr.put(p.getId(), initRank);
        }
        // Iterative computation
        for (int i = 0; i < PR_ITERATIONS; i++) {
            Map<Long, Double> newPr = new HashMap<>();
            for (Page p : allPages) {
                double rankSum = 0.0;
                // Sum contributions from inbound links
                for (Page q : allPages) {
                    Set<Long> qOut = outLinks.getOrDefault(q.getId(), Collections.emptySet());
                    if (qOut.contains(p.getId())) {
                        rankSum += pr.get(q.getId()) / qOut.size();
                    }
                }
                double updated = (1 - DAMPING) / n + DAMPING * rankSum;
                newPr.put(p.getId(), updated);
            }
            pr = newPr;
        }
        return pr;
    }
}
