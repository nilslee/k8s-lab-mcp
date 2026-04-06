package com.nilslee.mcp.config.logs;

import com.nilslee.mcp.service.logs.query.LogQueries;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Registers the Loki {@link LogQueries} client using Spring Framework’s
 * {@link ImportHttpServices @ImportHttpServices}.
 *
 * <p> This configuration contributes a {@link RestClientHttpServiceGroupConfigurer} scoped only to the {@code loki}
 * group so optional Grafana credentials stay isolated if more HTTP service clients are added later.
 */
@Configuration
@EnableConfigurationProperties(LokiConfigurationProperties.class)
@ImportHttpServices(group = "loki", types = LogQueries.class)
public class LokiHttpClientConfiguration {

  @Bean
  RestClientHttpServiceGroupConfigurer lokiGrafanaBasicAuthConfigurer(LokiConfigurationProperties props) {
    return groups ->
        groups
            .filterByName("loki")
            .forEachClient(
                (group, builder) -> {
                  String user = props.username();
                  if (user != null && !user.isBlank()) {
                    String pass = props.password() != null ? props.password() : "";
                    builder.defaultHeaders(h -> h.setBasicAuth(user, pass));
                  }
                });
  }
}
