package com.example.searchengine.indexer;

import com.example.searchengine.common.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple thread‑safe in‑memory inverted index.
 *
 * word -> (pageId -> term frequency)
 * Also tracks document frequency for each word and total document count.
 */
@Component
public class InvertedIndex {
    // word -> (pageId -> tf)
    private final Map<String, Map<Long, Integer>> index = Collections.synchronizedMap(new HashMap<>());
    // word -> document frequency (number of docs containing the word)
    private final Map<String, Integer> docFreq = Collections.synchronizedMap(new HashMap<>());
    private volatile int totalDocs = 0;

    /** Add or update a page in the index. */
    public void addPage(Page page) {
        long pageId = page.getId();
        // Tokenize the page content (excluding the link delimiter part for simplicity)
        Set<String> tokens = com.example.searchengine.util.TokenizerUtil.tokenize(page.getContent());
        // Update total document count if this is a new page
        synchronized (this) {
            totalDocs++;
        }
        // For each token, update term frequency and document frequency
        for (String token : tokens) {
            index.computeIfAbsent(token, k -> Collections.synchronizedMap(new HashMap<>()))
                 .merge(pageId, 1, Integer::sum);
            docFreq.merge(token, 1, Integer::sum);
        }
    }

    public Map<Long, Integer> getTermFrequencies(String token) {
        return index.getOrDefault(token, Collections.emptyMap());
    }

    public int getDocumentFrequency(String token) {
        return docFreq.getOrDefault(token, 0);
    }

    public int getTotalDocuments() {
        return totalDocs;
    }
}
