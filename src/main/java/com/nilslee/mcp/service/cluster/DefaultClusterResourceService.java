package com.nilslee.mcp.service.cluster;

import com.nilslee.mcp.model.cluster.InvolvedObjectKind;
import com.nilslee.mcp.model.cluster.PodPhaseFilter;
import com.nilslee.mcp.service.cluster.format.ClusterResourceTextFormatter;
import com.nilslee.mcp.service.cluster.query.ClusterResourceQueries;
import com.nilslee.mcp.service.cluster.validation.ClusterResourceInputValidator;
import com.nilslee.mcp.config.cluster.McpKubernetesProperties;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ClusterResourceService}.
 * Coordinates validation → Kubernetes API query → text formatting for MCP tools.
 * Secret helpers ({@link #getSecretValue}, {@link #setSecret}) decode {@code data} entries and write via
 * {@code stringData}; validation failures never reach the query layer.
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

    /** {@inheritDoc} */
    @Override
    public String clusterStatus() {
        return formatter.formatNodeList(queries.listNodes());
    }

    /** {@inheritDoc} */
    @Override
    public String listNamespaces() {
        return formatter.formatNamespaceList(queries.listNamespaces());
    }

    /** {@inheritDoc} */
    @Override
    public String listPods(@Nullable String namespace, @Nullable String labelSelector,
                           @Nullable PodPhaseFilter phaseFilter) {
        validator.validateNamespace(namespace);
        validator.validateLabelSelector(labelSelector);

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

    /** {@inheritDoc} */
    @Override
    public String describePod(String namespace, String podName) {
        validator.validateRequiredNamespace(namespace);
        validator.validateRequiredResourceName(podName, "podName");

        return queries.getPod(namespace, podName)
                .map(pod -> {
                    List<Event> events = queries.listEvents(namespace, "Pod", podName, props.maxEvents());
                    return formatter.formatPodDetail(pod, events);
                })
                .orElse("Pod '" + podName + "' not found in namespace '" + namespace + "'.");
    }

    /** {@inheritDoc} */
    @Override
    public String listDeployments(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        return formatter.formatDeploymentList(queries.listDeployments(namespace));
    }

    /** {@inheritDoc} */
    @Override
    public String listServices(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        return formatter.formatServiceList(queries.listServices(namespace));
    }

    /** {@inheritDoc} */
    @Override
    public String getEvents(@Nullable String namespace, @Nullable InvolvedObjectKind involvedObjectKind,
                            @Nullable String involvedObjectName) {
        validator.validateNamespace(namespace);
        validator.validateResourceName(involvedObjectName, "involvedObjectName");

        String kindStr = involvedObjectKind != null ? involvedObjectKind.name() : null;
        List<Event> events = queries.listEvents(namespace, kindStr, involvedObjectName, props.maxEvents());
        return formatter.formatEventList(events);
    }

    /** {@inheritDoc} */
    @Override
    public String topNodes() {
        try {
            return formatter.formatNodeMetrics(queries.topNodes());
        } catch (KubernetesClientException e) {
            log.warn("metrics-server unavailable for top-nodes: {}", e.getMessage());
            return "Node metrics unavailable: metrics-server may not be installed or reachable. Error: " + e.getMessage();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String topPods(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        try {
            return formatter.formatPodMetrics(queries.topPods(namespace));
        } catch (KubernetesClientException e) {
            log.warn("metrics-server unavailable for top-pods: {}", e.getMessage());
            return "Pod metrics unavailable: metrics-server may not be installed or reachable. Error: " + e.getMessage();
        }
    }


    /** {@inheritDoc} */
    @Override
    public SecretList listSecrets(@Nullable String namespace) {
        validator.validateNamespace(namespace);
        return queries.listSecrets(namespace);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getSecretValue(String namespace, String secretName, String keyName) {
        validator.validateRequiredNamespace(namespace);
        validator.validateRequiredResourceName(secretName, "secretName");
        validator.validateRequiredResourceName(keyName, "keyName");
        Secret secret = queries.getSecret(namespace, secretName);
        if (secret == null) {
            return null;
        }
        if (secret.getStringData() != null) {
            String plain = secret.getStringData().get(keyName);
            if (plain != null) {
                return plain;
            }
        }
        if (secret.getData() == null) {
            return null;
        }
        String encoded = secret.getData().get(keyName);
        if (encoded == null) {
            return null;
        }
        return decodeSecretDataEntry(encoded);
    }

    /** {@inheritDoc} */
    @Override
    public Secret setSecret(String namespace, String secretName, Map<String, String> secretMap) {
        validator.validateRequiredNamespace(namespace);
        validator.validateRequiredResourceName(secretName, "secretName");
        Secret secret =
                new SecretBuilder()
                        .withNewMetadata()
                        .withName(secretName)
                        .endMetadata()
                        .withStringData(new LinkedHashMap<>(secretMap))
                        .build();
        return queries.setSecret(namespace, secret);
    }

    private static String decodeSecretDataEntry(String dataValue) {
        try {
            return new String(Base64.getDecoder().decode(dataValue), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return dataValue;
        }
    }

}
