package com.nilslee.mcp.service.cluster;

import com.nilslee.mcp.model.cluster.InvolvedObjectKind;
import com.nilslee.mcp.model.cluster.PodPhaseFilter;
import org.jspecify.annotations.Nullable;

/**
 * Façade used by MCP tools. Orchestrates validation → query → format.
 * Returns human-readable strings suitable for the model.
 */
public interface ClusterResourceService {

    /**
     * Node conditions, versions, and Ready/NotReady summary for the cluster.
     *
     * @return formatted text for the model
     */
    String clusterStatus();

    /**
     * All namespaces with phase and age.
     *
     * @return formatted text for the model
     */
    String listNamespaces();

    /**
     * Pods across the cluster or one namespace, optionally filtered by labels and phase.
     *
     * @param namespace     {@code null} or blank = all namespaces (subject to allow-list config)
     * @param labelSelector {@code null} or blank = no filter; single {@code key=value} token only
     * @param phaseFilter   {@code null} or {@link PodPhaseFilter#ALL} = no phase filter
     * @return formatted text for the model
     */
    String listPods(@Nullable String namespace, @Nullable String labelSelector,
                    @Nullable PodPhaseFilter phaseFilter);

    /**
     * Rich pod detail including recent events.
     *
     * @param namespace required; Kubernetes namespace
     * @param podName   required; pod metadata name
     * @return formatted text for the model, or a not-found message
     */
    String describePod(String namespace, String podName);

    /**
     * Deployments and replica/image rollout status.
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted text for the model
     */
    String listDeployments(@Nullable String namespace);

    /**
     * Services (type, ClusterIP, ports).
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted text for the model
     */
    String listServices(@Nullable String namespace);

    /**
     * Recent events, optionally scoped to one involved object.
     *
     * @param namespace          {@code null} or blank = all namespaces
     * @param involvedObjectKind {@code null} = any kind
     * @param involvedObjectName {@code null} or blank = any name
     * @return formatted text for the model
     */
    String getEvents(@Nullable String namespace, @Nullable InvolvedObjectKind involvedObjectKind,
                     @Nullable String involvedObjectName);

    /**
     * Per-node CPU and memory (metrics-server).
     *
     * @return formatted text for the model, or an error hint if metrics are unavailable
     */
    String topNodes();

    /**
     * Per-pod CPU and memory (metrics-server).
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted text for the model, or an error hint if metrics are unavailable
     */
    String topPods(@Nullable String namespace);
}
