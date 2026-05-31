package com.mhtpsd.searchengine.api;

import com.mhtpsd.searchengine.common.Page;
import com.mhtpsd.searchengine.ranking.RankingService;
import com.mhtpsd.searchengine.util.SnippetUtil;
import com.mhtpsd.searchengine.util.TokenizerUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class SearchController {

    private final RankingService rankingService;

    public SearchController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Search endpoint.
     *
     * @param q   query string (required)
     * @param size number of results to return (optional, default 10)
     * @return list of SearchResult objects with highlighted snippets
     */
    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam("q") String q,
                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        // Get ranked pages
        List<Page> pages = rankingService.rank(q, size);
        Set<String> queryTokens = TokenizerUtil.tokenize(q);
        // Transform to DTOs with snippets
        return pages.stream()
                .map(p -> {
                    String snippet = SnippetUtil.generateSnippet(p.getContent(), queryTokens);
                    return new SearchResult(p.getUrl(), p.getTitle(), snippet);
                })
                .collect(Collectors.toList());
    }
}
