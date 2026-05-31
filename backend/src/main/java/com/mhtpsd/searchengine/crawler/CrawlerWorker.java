package com.mhtpsd.searchengine.crawler;

import com.mhtpsd.searchengine.common.Page;
import com.mhtpsd.searchengine.common.PageRepository;
import com.mhtpsd.searchengine.parser.ParserService;
import com.mhtpsd.searchengine.indexer.IndexerService;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CrawlerWorker {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerWorker.class);

    private final PageRepository pageRepository;
    private final ParserService parserService;
    private final IndexerService indexerService;
    private final Subscriber subscriber;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Value("${seed.urls:}")
    private String seedUrls; // comma‑separated list for local fallback

    public CrawlerWorker(PageRepository pageRepository,
                         ParserService parserService,
                         IndexerService indexerService,
                         Subscriber subscriber) {
        this.pageRepository = pageRepository;
        this.parserService = parserService;
        this.indexerService = indexerService;
        this.subscriber = subscriber;
    }

    @PostConstruct
    public void start() {
        // Start Pub/Sub subscriber in background
        try {
            subscriber.startAsync().awaitRunning();
            logger.info("Pub/Sub subscriber started");
        } catch (Exception e) {
            logger.warn("Failed to start Pub/Sub subscriber, will use seed URLs fallback", e);
        }
        // Schedule fallback processing of seed URLs every minute if no messages received
        executor.scheduleAtFixedRate(this::processSeedUrls, 0, 1, TimeUnit.MINUTES);
    }

    private void processSeedUrls() {
        if (seedUrls == null || seedUrls.isBlank()) {
            return;
        }
        for (String url : seedUrls.split(",")) {
            url = url.trim();
            if (!url.isEmpty()) {
                crawlAndStore(url);
            }
        }
    }

    // This method is invoked by Pub/Sub message receiver (configured in PubSubConfig)
    public void handleMessage(String url) {
        crawlAndStore(url);
    }

    private void crawlAndStore(String url) {
        try {
            Page page = parserService.fetchAndParse(url);
            // Save page (upsert)
            pageRepository.findByUrl(url).ifPresentOrElse(existing -> {
                existing.setTitle(page.getTitle());
                existing.setContent(page.getContent());
                existing.setFetchedAt(page.getFetchedAt());
                pageRepository.save(existing);
            }, () -> pageRepository.save(page));
            // Index the page
            indexerService.indexPage(page);
            logger.info("Crawled and indexed URL: {}", url);
        } catch (Exception e) {
            logger.error("Failed to crawl URL {}", url, e);
        }
    }
}
