package com.nilslee.mcp.config.log;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional Grafana basic authentication for the Loki HTTP service client.
 *
 * <p>Base URL and timeouts for that client are configured under
 * {@code spring.http.serviceclient.loki} (see Spring Boot’s HTTP Service Client support and
 *
 * @param username Optional user; when blank, no {@code Authorization} header is added.
 * @param password Optional password (e.g. bind from {@code GRAFANA_PASSWORD}).
 */
@ConfigurationProperties(prefix = "mcp.monitoring.loki")
public record LokiConfigurationProperties(@Nullable String username, @Nullable String password) {}
