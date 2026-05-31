package com.mhtpsd.searchengine.util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates a short snippet from page content that contains one of the query tokens.
 * Highlights matched terms by surrounding them with <b> tags.
 */
public class SnippetUtil {

    private static final int SNIPPET_LENGTH = 200; // characters

    public static String generateSnippet(String content, Set<String> queryTokens) {
        if (content == null || content.isEmpty() || queryTokens.isEmpty()) {
            return "";
        }
        // Build a regex that matches any token (word boundaries, case‑insensitive)
        String patternStr = queryTokens.stream()
                .map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");
        Pattern pattern = Pattern.compile("\\b(" + patternStr + ")\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            int start = Math.max(0, matcher.start() - SNIPPET_LENGTH / 2);
            int end = Math.min(content.length(), start + SNIPPET_LENGTH);
            String snippet = content.substring(start, end);
            // Highlight all occurrences of query tokens in the snippet
            Matcher highlightMatcher = pattern.matcher(snippet);
            StringBuffer sb = new StringBuffer();
            while (highlightMatcher.find()) {
                highlightMatcher.appendReplacement(sb, "<b>" + highlightMatcher.group(1) + "</b>");
            }
            highlightMatcher.appendTail(sb);
            return sb.toString() + (end < content.length() ? "..." : "");
        }
        // Fallback: first SNIPPET_LENGTH chars
        return content.substring(0, Math.min(SNIPPET_LENGTH, content.length())) + (content.length() > SNIPPET_LENGTH ? "..." : "");
    }
}
