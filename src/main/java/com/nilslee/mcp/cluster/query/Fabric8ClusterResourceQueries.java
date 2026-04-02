package com.nilslee.mcp.cluster.query;

import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fabric8-based implementation. This is the ONLY class that calls KubernetesClient.
 * All operations are strictly read-only (list/get). No create/update/delete/patch,
 * no pod exec, no port-forward, no arbitrary API paths.
 */
@Component
public class Fabric8ClusterResourceQueries implements ClusterResourceQueries {

    private final KubernetesClient client;

    public Fabric8ClusterResourceQueries(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public List<Node> listNodes() {
        return client.nodes().list().getItems();
    }

    @Override
    public List<Namespace> listNamespaces() {
        return client.namespaces().list().getItems();
    }

    @Override
    public List<Pod> listPods(@Nullable String namespace, @Nullable String labelSelector) {
        var resource = isBlank(namespace)
                ? client.pods().inAnyNamespace()
                : client.pods().inNamespace(namespace);

        if (!isBlank(labelSelector)) {
            return resource.withLabelSelector(labelSelector).list().getItems();
        }
        return resource.list().getItems();
    }

    @Override
    public Optional<Pod> getPod(String namespace, String podName) {
        return Optional.ofNullable(client.pods().inNamespace(namespace).withName(podName).get());
    }

    @Override
    public List<Event> listEvents(@Nullable String namespace, @Nullable String involvedObjectKind,
                                  @Nullable String involvedObjectName, int limit) {
        List<String> fieldParts = new ArrayList<>();
        if (!isBlank(involvedObjectKind)) {
            fieldParts.add("involvedObject.kind=" + involvedObjectKind);
        }
        if (!isBlank(involvedObjectName)) {
            fieldParts.add("involvedObject.name=" + involvedObjectName);
        }

        ListOptionsBuilder optsBuilder = new ListOptionsBuilder().withLimit((long) limit);
        if (!fieldParts.isEmpty()) {
            optsBuilder.withFieldSelector(String.join(",", fieldParts));
        }
        ListOptions opts = optsBuilder.build();

        return isBlank(namespace)
                ? client.v1().events().inAnyNamespace().list(opts).getItems()
                : client.v1().events().inNamespace(namespace).list(opts).getItems();
    }

    @Override
    public List<Deployment> listDeployments(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.apps().deployments().inAnyNamespace().list().getItems()
                : client.apps().deployments().inNamespace(namespace).list().getItems();
    }

    @Override
    public List<Service> listServices(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.services().inAnyNamespace().list().getItems()
                : client.services().inNamespace(namespace).list().getItems();
    }

    @Override
    public NodeMetricsList topNodes() {
        return client.top().nodes().metrics();
    }

    @Override
    public PodMetricsList topPods(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.top().pods().metrics()
                : client.top().pods().inNamespace(namespace).metrics();
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
