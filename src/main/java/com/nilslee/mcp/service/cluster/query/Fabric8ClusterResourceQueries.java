package com.nilslee.mcp.service.cluster.query;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fabric8-based implementation. This is the only class that calls {@link KubernetesClient}.
 * Most operations are list/get reads. {@link #setSecret} performs create-or-replace for
 * MCP-managed secrets (for example Argo CD bootstrap credentials written at runtime).
 */
@Component
public class Fabric8ClusterResourceQueries implements ClusterResourceQueries {

    private final KubernetesClient client;

    public Fabric8ClusterResourceQueries(KubernetesClient client) {
        this.client = client;
    }

    /** {@inheritDoc} */
    @Override
    public List<Node> listNodes() {
        return client.nodes().list().getItems();
    }

    /** {@inheritDoc} */
    @Override
    public List<Namespace> listNamespaces() {
        return client.namespaces().list().getItems();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Optional<Pod> getPod(String namespace, String podName) {
        return Optional.ofNullable(client.pods().inNamespace(namespace).withName(podName).get());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public List<Deployment> listDeployments(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.apps().deployments().inAnyNamespace().list().getItems()
                : client.apps().deployments().inNamespace(namespace).list().getItems();
    }

    /** {@inheritDoc} */
    @Override
    public List<Service> listServices(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.services().inAnyNamespace().list().getItems()
                : client.services().inNamespace(namespace).list().getItems();
    }

    /** {@inheritDoc} */
    @Override
    public NodeMetricsList topNodes() {
        return client.top().nodes().metrics();
    }

    /** {@inheritDoc} */
    @Override
    public PodMetricsList topPods(@Nullable String namespace) {
        return isBlank(namespace)
                ? client.top().pods().metrics()
                : client.top().pods().inNamespace(namespace).metrics();
    }

    /** {@inheritDoc} */
    @Override
    public SecretList listSecrets(@Nullable String namespace) {
        return isBlank(namespace)
            ? client.secrets().inAnyNamespace().list()
            : client.secrets().inNamespace(namespace).list();
    }

    /** {@inheritDoc} */
    @Override
    public Secret getSecret(String namespace, String secretName) {
        return client.secrets().inNamespace(namespace).withName(secretName).get();
    }

    /** {@inheritDoc} */
    @Override
    public Secret setSecret(String namespace, Secret secret) {
        return client.secrets().inNamespace(namespace).resource(secret).createOrReplace();
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
