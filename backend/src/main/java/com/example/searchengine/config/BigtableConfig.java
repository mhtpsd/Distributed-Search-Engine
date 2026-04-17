package com.example.searchengine.config;

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BigtableConfig {

    @Bean
    public BigtableDataClient bigtableDataClient() throws Exception {
        String projectId = System.getenv("GCP_PROJECT_ID");
        String instanceId = System.getenv("BIGTABLE_INSTANCE_ID");
        BigtableDataSettings settings = BigtableDataSettings.newBuilder()
                .setProjectId(projectId)
                .setInstanceId(instanceId)
                .build();
        return BigtableDataClient.create(settings);
    }
}
