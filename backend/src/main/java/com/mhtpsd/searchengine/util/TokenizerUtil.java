package com.mhtpsd.searchengine.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TokenizerUtil {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "while", "with",
            "of", "at", "by", "for", "to", "in", "on", "from", "up", "down",
            "is", "are", "was", "were", "be", "been", "being", "has", "have",
            "had", "do", "does", "did", "not", "no", "yes", "can", "could",
            "should", "would", "may", "might", "will", "just", "so", "than"
    ));

    /**
     * Splits the given text into lowercase tokens, removes stop‑words and non‑alphabetic characters.
     */
    public static Set<String> tokenize(String text) {
        if (text == null) {
            return Set.of();
        }
        return Arrays.stream(text.split("\\W+"))
                .map(String::toLowerCase)
                .filter(token -> !token.isBlank() && !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }
}
