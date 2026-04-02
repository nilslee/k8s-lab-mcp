package com.nilslee.mcp.cluster;

import com.nilslee.mcp.cluster.format.ClusterResourceTextFormatter;
import com.nilslee.mcp.cluster.query.ClusterResourceQueries;
import com.nilslee.mcp.cluster.validation.ClusterResourceInputValidator;
import com.nilslee.mcp.cluster.validation.InvalidClusterInputException;
import com.nilslee.mcp.config.McpKubernetesProperties;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ClusterResourceService}.
 * Coordinates validation → Kubernetes API query → text formatting.
 * Validation failures never reach the query layer.
 */
@Service
public class DefaultClusterResourceService implements ClusterResourceService {

    private static final Logger log = LoggerFactory.getLogger(DefaultClusterResourceService.class);

    private final ClusterResourceInputValidator validator;
    private final ClusterResourceQueries queries;
    private final ClusterResourceTextFormatter formatter;
    private final McpKubernetesProperties props;

    public DefaultClusterResourceService(ClusterResourceInputValidator validator,
                                         ClusterResourceQueries queries,
                                         ClusterResourceTextFormatter formatter,
                                         McpKubernetesProperties props) {
        this.validator = validator;
        this.queries = queries;
        this.formatter = formatter;
        this.props = props;
    }

    @Override
    public String clusterStatus() {
        return formatter.formatNodeList(queries.listNodes());
    }

    @Override
    public String listNamespaces() {
        return formatter.formatNamespaceList(queries.listNamespaces());
    }

    @Override
    public String listPods(@Nullable String namespace, @Nullable String labelSelector,
                           @Nullable PodPhaseFilter phaseFilter) {
        validator.validateNamespace(namespace);
        validator.validateLabelSelector(labelSelector);
        checkNamespaceAllowed(namespace);

        List<Pod> pods = queries.listPods(namespace, labelSelector);
        if (phaseFilter != null && phaseFilter != PodPhaseFilter.ALL) {
            String phase = phaseFilter.name().charAt(0)
                    + phaseFilter.name().substring(1).toLowerCase();
            pods = pods.stream()
                    .filter(p -> phase.equals(
                            p.getStatus() != null ? p.getStatus().getPhase() : null))
                    .collect(Collectors.toList());
        }
        return formatter.formatPodList(pods);
    }

    @Override
    public String describePod(String namespace, String podName) {
        validator.validateRequiredNamespace(namespace);
        validator.validateRequiredResourceName(podName, "podName");
        checkNamespaceAllowed(namespace);

        return queries.getPod(namespace, podName)
                .map(pod -> {
                    List<Event> events = queries.listEvents(namespace, "Pod", podName, props.getMaxEvents());
                    return formatter.formatPodDetail(pod, events);
                })
                .orElse("Pod '" + podName + "' not found in namespace '" + namespace + "'.");
    }

    @Override
    public String listDeployments(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        checkNamespaceAllowed(namespace);
        return formatter.formatDeploymentList(queries.listDeployments(namespace));
    }

    @Override
    public String listServices(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        checkNamespaceAllowed(namespace);
        return formatter.formatServiceList(queries.listServices(namespace));
    }

    @Override
    public String getEvents(@Nullable String namespace, @Nullable InvolvedObjectKind involvedObjectKind,
                            @Nullable String involvedObjectName) {
        validator.validateNamespace(namespace);
        validator.validateResourceName(involvedObjectName, "involvedObjectName");
        checkNamespaceAllowed(namespace);

        String kindStr = involvedObjectKind != null ? involvedObjectKind.name() : null;
        List<Event> events = queries.listEvents(namespace, kindStr, involvedObjectName, props.getMaxEvents());
        return formatter.formatEventList(events);
    }

    @Override
    public String topNodes() {
        try {
            return formatter.formatNodeMetrics(queries.topNodes());
        } catch (KubernetesClientException e) {
            log.warn("metrics-server unavailable for top-nodes: {}", e.getMessage());
            return "Node metrics unavailable: metrics-server may not be installed or reachable. Error: " + e.getMessage();
        }
    }

    @Override
    public String topPods(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        checkNamespaceAllowed(namespace);
        try {
            return formatter.formatPodMetrics(queries.topPods(namespace));
        } catch (KubernetesClientException e) {
            log.warn("metrics-server unavailable for top-pods: {}", e.getMessage());
            return "Pod metrics unavailable: metrics-server may not be installed or reachable. Error: " + e.getMessage();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void checkNamespaceAllowed(@Nullable String namespace) {
        List<String> allowed = props.getAllowedNamespaces();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        if (namespace == null || namespace.isBlank()) {
            return;
        }
        if (!allowed.contains(namespace)) {
            throw new InvalidClusterInputException(
                    "Namespace '" + namespace + "' is not in the configured allowed-namespaces list.");
        }
    }
}
