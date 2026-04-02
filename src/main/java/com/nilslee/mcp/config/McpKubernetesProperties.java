package com.nilslee.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mcp.kubernetes")
public class McpKubernetesProperties {

    /**
     * Optional allowlist of namespaces. When non-empty, only these namespaces
     * may be queried. Empty list means "allow all namespaces the kubeconfig can read."
     */
    private List<String> allowedNamespaces = new ArrayList<>();

    /**
     * Maximum number of events returned by get-events tool.
     */
    private int maxEvents = 500;

    /**
     * Timeout for establishing a connection to the Kubernetes API server.
     */
    private Duration connectionTimeout = Duration.ofSeconds(10);

    /**
     * Timeout for reading a response from the Kubernetes API server.
     */
    private Duration readTimeout = Duration.ofSeconds(30);

    public List<String> getAllowedNamespaces() {
        return allowedNamespaces;
    }

    public void setAllowedNamespaces(List<String> allowedNamespaces) {
        this.allowedNamespaces = allowedNamespaces;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}
