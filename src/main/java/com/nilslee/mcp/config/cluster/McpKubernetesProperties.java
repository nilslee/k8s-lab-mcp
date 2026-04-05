package com.nilslee.mcp.config.cluster;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Kubernetes configuration properties for the MCP application.
 *
 * @param maxEvents         Maximum number of events returned by get-events tool.
 * @param connectionTimeout Timeout for establishing a connection to the Kubernetes API server.
 * @param readTimeout       Timeout for reading a response from the Kubernetes API server.
 */
@ConfigurationProperties(prefix = "mcp.kubernetes")
public record McpKubernetesProperties(
    int maxEvents,
    Duration connectionTimeout,
    Duration readTimeout
) {
  public McpKubernetesProperties {
    if (maxEvents == 0) {
      maxEvents = 500;
    }
    if (connectionTimeout == null) {
      connectionTimeout = Duration.ofSeconds(10);
    }
    if (readTimeout == null) {
      readTimeout = Duration.ofSeconds(30);
    }
  }
}
