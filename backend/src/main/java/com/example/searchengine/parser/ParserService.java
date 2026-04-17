package com.example.searchengine.parser;

import com.example.searchengine.common.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ParserService {

    /**
     * Fetches the HTML content of the given URL and extracts title, text, and outbound links.
     * Returns a {@link Page} entity ready for persistence.
     */
    public Page fetchAndParse(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; DistributedSearchEngine/1.0)")
                .timeout(10_000)
                .get();
        String title = doc.title();
        String text = doc.body().text();
        // Store links as a simple newline‑separated string for now
        Elements linkElements = doc.select("a[href]");
        List<String> links = new ArrayList<>();
        linkElements.forEach(e -> links.add(e.absUrl("href")));
        String linksJoined = String.join("\n", links);
        // For simplicity we embed links in the content field after a delimiter
        String contentWithLinks = text + "\n\n---LINKS---\n" + linksJoined;
        return new Page(url, title, contentWithLinks, LocalDateTime.now());
    }
}
