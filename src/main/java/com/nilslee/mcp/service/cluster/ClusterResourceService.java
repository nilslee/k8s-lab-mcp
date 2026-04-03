package com.nilslee.mcp.service.cluster;

import com.nilslee.mcp.model.cluster.InvolvedObjectKind;
import com.nilslee.mcp.model.cluster.PodPhaseFilter;
import org.springframework.lang.Nullable;

/**
 * Façade used by MCP tools. Orchestrates validation → query → format.
 * Returns human-readable strings suitable for the model.
 */
public interface ClusterResourceService {

    String clusterStatus();

    String listNamespaces();

    /**
     * @param namespace     null/blank = all namespaces
     * @param labelSelector null/blank = no filter; single {@code key=value}
     * @param phaseFilter   null or {@link PodPhaseFilter#ALL} = no phase filter
     */
    String listPods(@Nullable String namespace, @Nullable String labelSelector,
                    @Nullable PodPhaseFilter phaseFilter);

    /**
     * @param namespace required
     * @param podName   required
     */
    String describePod(String namespace, String podName);

    /**
     * @param namespace null/blank = all namespaces
     */
    String listDeployments(@Nullable String namespace);

    /**
     * @param namespace null/blank = all namespaces
     */
    String listServices(@Nullable String namespace);

    /**
     * @param namespace           null/blank = all namespaces
     * @param involvedObjectKind  null = any kind
     * @param involvedObjectName  null/blank = any name
     */
    String getEvents(@Nullable String namespace, @Nullable InvolvedObjectKind involvedObjectKind,
                     @Nullable String involvedObjectName);

    String topNodes();

    /**
     * @param namespace null/blank = all namespaces
     */
    String topPods(@Nullable String namespace);
}
