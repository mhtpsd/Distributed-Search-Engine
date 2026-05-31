package com.mhtpsd.searchengine.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerUtilTest {

    @Test
    void tokenize_normalSentence_returnsLowercaseTokens() {
        Set<String> tokens = TokenizerUtil.tokenize("Hello World Java");
        assertTrue(tokens.contains("hello"));
        assertTrue(tokens.contains("world"));
        assertTrue(tokens.contains("java"));
    }

    @Test
    void tokenize_stopWordsRemoved() {
        Set<String> tokens = TokenizerUtil.tokenize("the quick brown fox");
        assertFalse(tokens.contains("the"));
        assertTrue(tokens.contains("quick"));
        assertTrue(tokens.contains("brown"));
        assertTrue(tokens.contains("fox"));
    }

    @Test
    void tokenize_emptyString_returnsEmptySet() {
        Set<String> tokens = TokenizerUtil.tokenize("");
        assertTrue(tokens.isEmpty());
    }

    @Test
    void tokenize_punctuationStripped() {
        Set<String> tokens = TokenizerUtil.tokenize("Hello, World! Java.");
        assertTrue(tokens.contains("hello"));
        assertTrue(tokens.contains("world"));
        assertTrue(tokens.contains("java"));
        tokens.forEach(t -> assertTrue(t.matches("[a-z]+")));
    }
}
