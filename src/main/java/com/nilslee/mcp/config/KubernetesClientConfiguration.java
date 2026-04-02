package com.nilslee.mcp.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(McpKubernetesProperties.class)
public class KubernetesClientConfiguration {

    @Bean
    public KubernetesClient kubernetesClient(McpKubernetesProperties props) {
        Config config = new ConfigBuilder()
                .withConnectionTimeout((int) props.getConnectionTimeout().toMillis())
                .withRequestTimeout((int) props.getReadTimeout().toMillis())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }
}
