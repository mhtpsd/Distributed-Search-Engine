package com.mhtpsd.searchengine.ranking;

import com.mhtpsd.searchengine.common.Page;
import com.mhtpsd.searchengine.common.PageRepository;
import com.mhtpsd.searchengine.indexer.IndexerService;
import com.mhtpsd.searchengine.indexer.InvertedIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private IndexerService indexerService;

    @Mock
    private InvertedIndex invertedIndex;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private RankingService rankingService;

    @Test
    void rank_emptySearchResult_returnsEmptyList() {
        when(indexerService.search(anyString(), anyInt())).thenReturn(Collections.emptyList());

        List<Page> results = rankingService.rank("test", 10);

        assertTrue(results.isEmpty());
    }

    @Test
    void rank_pagesReturnedOrderedByScoreDescending() {
        Page pageA = new Page("http://a.com", "java programming title", "java programming content ---LINKS---", LocalDateTime.now());
        pageA.setId(1L);
        Page pageB = new Page("http://b.com", "other title", "unrelated content ---LINKS---", LocalDateTime.now());
        pageB.setId(2L);

        when(indexerService.search(anyString(), anyInt())).thenReturn(List.of(1L, 2L));
        when(indexerService.getPagesByIds(anyList())).thenReturn(List.of(pageA, pageB));
        when(pageRepository.findAll()).thenReturn(List.of(pageA, pageB));

        when(indexerService.getInvertedIndex()).thenReturn(invertedIndex);
        when(invertedIndex.getTotalDocuments()).thenReturn(2);
        when(invertedIndex.getTermFrequencies("java")).thenReturn(Map.of(1L, 3, 2L, 0));
        when(invertedIndex.getDocumentFrequency("java")).thenReturn(1);
        when(invertedIndex.getTermFrequencies("programming")).thenReturn(Map.of(1L, 2, 2L, 0));
        when(invertedIndex.getDocumentFrequency("programming")).thenReturn(1);

        List<Page> results = rankingService.rank("java programming", 10);

        assertFalse(results.isEmpty());
        // pageA should rank higher: it has "java" and "programming" in both title and content
        assertEquals("http://a.com", results.get(0).getUrl());
    }
}
