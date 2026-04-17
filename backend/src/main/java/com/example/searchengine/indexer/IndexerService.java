package com.example.searchengine.indexer;

import com.example.searchengine.common.Page;
import com.example.searchengine.common.PageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for indexing pages and performing simple TF‑IDF based search.
 * Uses the {@link InvertedIndex} component for term storage.
 */
@Service
public class IndexerService {

    private final InvertedIndex invertedIndex;
    private final PageRepository pageRepository;

    public IndexerService(InvertedIndex invertedIndex, PageRepository pageRepository) {
        this.invertedIndex = invertedIndex;
        this.pageRepository = pageRepository;
    }

    /**
     * Indexes (or re‑indexes) a page. This updates term frequencies and document frequencies.
     */
    public void indexPage(Page page) {
        invertedIndex.addPage(page);
    }

    /**
     * Perform a TF‑IDF search for the given query string and return the top N page IDs.
     * The result is sorted by descending TF‑IDF score.
     */
    public List<Long> search(String query, int topK) {
        Set<String> queryTokens = com.example.searchengine.util.TokenizerUtil.tokenize(query);
        if (queryTokens.isEmpty()) {
            return Collections.emptyList();
        }
        int totalDocs = invertedIndex.getTotalDocuments();
        // Compute TF‑IDF score per document
        Map<Long, Double> docScores = queryTokens.stream()
                .collect(Collectors.toMap(
                        token -> token,
                        token -> 0.0,
                        (a, b) -> a)) // dummy map just to iterate tokens
                .keySet()
                .stream()
                .flatMap(token -> {
                    Map<Long, Integer> tfMap = invertedIndex.getTermFrequencies(token);
                    int df = invertedIndex.getDocumentFrequency(token);
                    double idf = df == 0 ? 0.0 : Math.log((double) totalDocs / (double) df);
                    return tfMap.entrySet().stream()
                            .map(e -> new Object[]{e.getKey(), e.getValue() * idf});
                })
                .collect(Collectors.groupingBy(
                        arr -> (Long) arr[0],
                        Collectors.summingDouble(arr -> (Double) arr[1])
                ));
        // Sort and limit
        return docScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Helper to retrieve full Page objects for a list of IDs.
     */
    public List<Page> getPagesByIds(List<Long> ids) {
        return ids.stream()
                .map(pageRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
    }
}
