package com.mhtpsd.searchengine.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SnippetUtilTest {

    @Test
    void generateSnippet_containsQueryTerm() {
        String content = "The quick brown fox jumps over the lazy dog near the river bank";
        Set<String> tokens = Set.of("fox");
        String snippet = SnippetUtil.generateSnippet(content, tokens);
        assertTrue(snippet.toLowerCase().contains("fox"));
    }

    @Test
    void generateSnippet_notLongerThan200Chars() {
        String content = "word ".repeat(200);
        Set<String> tokens = Set.of("word");
        String snippet = SnippetUtil.generateSnippet(content, tokens);
        // Strip HTML tags for length check
        String plain = snippet.replaceAll("<[^>]+>", "").replace("...", "");
        assertTrue(plain.length() <= 200);
    }

    @Test
    void generateSnippet_nullContent_returnsEmpty() {
        String snippet = SnippetUtil.generateSnippet(null, Set.of("test"));
        assertEquals("", snippet);
    }

    @Test
    void generateSnippet_emptyContent_returnsEmpty() {
        String snippet = SnippetUtil.generateSnippet("", Set.of("test"));
        assertEquals("", snippet);
    }
}
