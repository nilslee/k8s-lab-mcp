package com.nilslee.mcp.service.cluster.query;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Read-only Kubernetes data access interface.
 * Only this interface (and its single implementation) touches the Kubernetes API.
 * All operations are list/get/watch-style reads; no mutations are permitted.
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
}
