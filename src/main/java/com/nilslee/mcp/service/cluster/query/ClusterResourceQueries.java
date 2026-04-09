package com.nilslee.mcp.service.cluster.query;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Kubernetes data access interface. Only this interface (and its single implementation)
 * calls the Kubernetes API directly. List/get operations are read-only; {@link #setSecret}
 * creates or replaces a {@link Secret} in the given namespace (used for MCP-owned secrets).
 */
public interface ClusterResourceQueries {

    /**
     * @return all nodes in the cluster
     */
    List<Node> listNodes();

    /**
     * @return all namespaces
     */
    List<Namespace> listNamespaces();

    /**
     * @param namespace     {@code null} or blank = all namespaces
     * @param labelSelector {@code null} or blank = no selector; format: {@code key=value}
     * @return pods matching scope and optional label selector
     */
    List<Pod> listPods(@Nullable String namespace, @Nullable String labelSelector);

    /**
     * @param namespace Kubernetes namespace
     * @param podName   pod metadata name
     * @return empty Optional if the pod does not exist
     */
    Optional<Pod> getPod(String namespace, String podName);

    /**
     * @param namespace          {@code null} or blank = all namespaces
     * @param involvedObjectKind {@code null} = any kind (Kubernetes kind string)
     * @param involvedObjectName {@code null} or blank = any name
     * @param limit              maximum number of events to return from the API list call
     * @return events, newest limited by {@code limit}
     */
    List<Event> listEvents(@Nullable String namespace, @Nullable String involvedObjectKind,
                           @Nullable String involvedObjectName, int limit);

    /**
     * @param namespace {@code null} or blank = all namespaces
     * @return deployments in scope
     */
    List<Deployment> listDeployments(@Nullable String namespace);

    /**
     * @param namespace {@code null} or blank = all namespaces
     * @return services in scope
     */
    List<Service> listServices(@Nullable String namespace);

    /**
     * @return node resource usage metrics
     * @throws io.fabric8.kubernetes.client.KubernetesClientException if metrics-server is unavailable
     */
    NodeMetricsList topNodes();

    /**
     * @param namespace {@code null} or blank = all namespaces
     * @return pod resource usage metrics in scope
     * @throws io.fabric8.kubernetes.client.KubernetesClientException if metrics-server is unavailable
     */
    PodMetricsList topPods(@Nullable String namespace);

    /**
     * @param namespace {@code null} or blank = all namespaces
     * @return secrets in scope (raw API model)
     */
    SecretList listSecrets(@Nullable String namespace);

    /**
     * @param namespace   secret namespace
     * @param secretName  metadata name
     * @return the secret, or {@code null} if missing
     */
    Secret getSecret(String namespace, String secretName);

    /**
     * Creates or replaces a secret in the namespace (idempotent for the same name).
     *
     * @param namespace target namespace
     * @param secret    model including metadata name and data/stringData
     * @return persisted secret as returned by the API
     */
    Secret setSecret(String namespace, Secret secret);
}
