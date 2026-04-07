package com.nilslee.mcp.config.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mcp.monitoring.prometheus")
public record PrometheusConfigurationProperties (
    // Empty for now
) {
}
