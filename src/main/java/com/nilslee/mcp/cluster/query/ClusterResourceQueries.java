package com.nilslee.mcp.cluster.query;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Read-only Kubernetes data access interface.
 * Only this interface (and its single implementation) touches the Kubernetes API.
 * All operations are list/get/watch-style reads; no mutations are permitted.
 */
public interface ClusterResourceQueries {

    List<Node> listNodes();

    List<Namespace> listNamespaces();

    /**
     * @param namespace     null or blank = all namespaces
     * @param labelSelector null or blank = no selector; format: {@code key=value}
     */
    List<Pod> listPods(@Nullable String namespace, @Nullable String labelSelector);

    /**
     * @return empty Optional if pod does not exist
     */
    Optional<Pod> getPod(String namespace, String podName);

    /**
     * @param namespace           null or blank = all namespaces
     * @param involvedObjectKind  null = any kind
     * @param involvedObjectName  null or blank = any name
     * @param limit               maximum number of events to return
     */
    List<Event> listEvents(@Nullable String namespace, @Nullable String involvedObjectKind,
                           @Nullable String involvedObjectName, int limit);

    /**
     * @param namespace null or blank = all namespaces
     */
    List<Deployment> listDeployments(@Nullable String namespace);

    /**
     * @param namespace null or blank = all namespaces
     */
    List<Service> listServices(@Nullable String namespace);

    /**
     * @throws io.fabric8.kubernetes.client.KubernetesClientException if metrics-server is unavailable
     */
    NodeMetricsList topNodes();

    /**
     * @param namespace null or blank = all namespaces
     * @throws io.fabric8.kubernetes.client.KubernetesClientException if metrics-server is unavailable
     */
    PodMetricsList topPods(@Nullable String namespace);
}
