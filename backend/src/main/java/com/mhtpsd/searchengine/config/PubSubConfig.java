package com.mhtpsd.searchengine.config;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PubSubConfig {

    @Bean
    public Publisher seedUrlPublisher() throws Exception {
        String projectId = System.getenv("GCP_PROJECT_ID");
        ProjectTopicName topicName = ProjectTopicName.of(projectId, "seed-urls");
        return Publisher.newBuilder(topicName).build();
    }

    @Bean
    public Publisher newUrlPublisher() throws Exception {
        String projectId = System.getenv("GCP_PROJECT_ID");
        ProjectTopicName topicName = ProjectTopicName.of(projectId, "new-urls");
        return Publisher.newBuilder(topicName).build();
    }

    @Bean
    public Subscriber newUrlSubscriber() throws Exception {
        String projectId = System.getenv("GCP_PROJECT_ID");
        ProjectSubscriptionName subscription = ProjectSubscriptionName.of(projectId, "new-urls-sub");
        // Placeholder message receiver; actual logic will be wired in CrawlerWorker
        MessageReceiver receiver = (message, consumer) -> {
            // No-op; real processing is in CrawlerWorker
            consumer.ack();
        };
        return Subscriber.newBuilder(subscription, receiver).build();
    }
}
