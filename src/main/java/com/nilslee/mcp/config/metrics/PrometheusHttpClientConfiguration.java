package com.nilslee.mcp.config.metrics;

import com.nilslee.mcp.service.metrics.query.PrometheusMetricQueries;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@EnableConfigurationProperties(PrometheusConfigurationProperties.class)
@ImportHttpServices(group = "prometheus", types = PrometheusMetricQueries.class)
public class PrometheusHttpClientConfiguration {

  @Bean
  RestClientHttpServiceGroupConfigurer prometheusBasicAuthConfigurer(PrometheusConfigurationProperties props) {
    return (groups -> groups
        .filterByName("prometheus")
        .forEachClient((group, clientBuilder) -> {
          // Empty for now
        }));
  }
}
