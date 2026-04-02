package com.nilslee.mcp.cluster;

import com.nilslee.mcp.cluster.format.ClusterResourceTextFormatter;
import com.nilslee.mcp.cluster.query.ClusterResourceQueries;
import com.nilslee.mcp.cluster.validation.ClusterResourceInputValidator;
import com.nilslee.mcp.cluster.validation.InvalidClusterInputException;
import com.nilslee.mcp.config.McpKubernetesProperties;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultClusterResourceServiceTest {

    @Mock
    private ClusterResourceQueries queries;

    @Mock
    private ClusterResourceTextFormatter formatter;

    private final ClusterResourceInputValidator validator = new ClusterResourceInputValidator();
    private final McpKubernetesProperties props = new McpKubernetesProperties();

    private DefaultClusterResourceService service;

    @BeforeEach
    void setUp() {
        service = new DefaultClusterResourceService(validator, queries, formatter, props);
    }

    // -------------------------------------------------------------------------
    // clusterStatus
    // -------------------------------------------------------------------------

    @Test
    void clusterStatus_delegatesToQueriesAndFormatter() {
        List<Node> nodes = List.of();
        when(queries.listNodes()).thenReturn(nodes);
        when(formatter.formatNodeList(nodes)).thenReturn("nodes output");

        String result = service.clusterStatus();

        assertThat(result).isEqualTo("nodes output");
    }

    // -------------------------------------------------------------------------
    // listPods – validation failures never reach queries
    // -------------------------------------------------------------------------

    @Test
    void listPods_invalidNamespace_neverCallsQueries() {
        assertThatThrownBy(() -> service.listPods("INVALID-NAMESPACE", null, null))
                .isInstanceOf(InvalidClusterInputException.class);
        verify(queries, never()).listPods(anyString(), any());
    }

    @Test
    void listPods_shellInjectionInNamespace_neverCallsQueries() {
        assertThatThrownBy(() -> service.listPods("ns; rm -rf /", null, null))
                .isInstanceOf(InvalidClusterInputException.class);
        verify(queries, never()).listPods(anyString(), any());
    }

    @Test
    void listPods_invalidLabelSelector_neverCallsQueries() {
        assertThatThrownBy(() -> service.listPods("default", "app=nginx,env=prod", null))
                .isInstanceOf(InvalidClusterInputException.class);
        verify(queries, never()).listPods(anyString(), any());
    }

    @Test
    void listPods_validParams_callsQueriesAndFormatsResult() {
        List<Pod> pods = List.of();
        when(queries.listPods("default", "app=nginx")).thenReturn(pods);
        when(formatter.formatPodList(pods)).thenReturn("pods output");

        String result = service.listPods("default", "app=nginx", null);

        assertThat(result).isEqualTo("pods output");
    }

    @Test
    void listPods_withPhaseFilter_filtersBeforeFormatting() {
        Pod runningPod = new PodBuilder()
                .withNewMetadata().withName("running-pod").withNamespace("default").endMetadata()
                .withNewStatus().withPhase("Running").endStatus()
                .build();
        Pod failedPod = new PodBuilder()
                .withNewMetadata().withName("failed-pod").withNamespace("default").endMetadata()
                .withNewStatus().withPhase("Failed").endStatus()
                .build();

        when(queries.listPods("default", null)).thenReturn(List.of(runningPod, failedPod));
        when(formatter.formatPodList(List.of(runningPod))).thenReturn("running only");

        String result = service.listPods("default", null, PodPhaseFilter.RUNNING);

        assertThat(result).isEqualTo("running only");
    }

    // -------------------------------------------------------------------------
    // describePod – requires namespace and podName
    // -------------------------------------------------------------------------

    @Test
    void describePod_missingNamespace_throwsBeforeQueryCall() {
        assertThatThrownBy(() -> service.describePod(null, "my-pod"))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
        verify(queries, never()).getPod(anyString(), anyString());
    }

    @Test
    void describePod_missingPodName_throwsBeforeQueryCall() {
        assertThatThrownBy(() -> service.describePod("default", ""))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
        verify(queries, never()).getPod(anyString(), anyString());
    }

    @Test
    void describePod_podNotFound_returnsNotFoundMessage() {
        when(queries.getPod("default", "missing-pod")).thenReturn(Optional.empty());

        String result = service.describePod("default", "missing-pod");

        assertThat(result).contains("not found");
        verify(formatter, never()).formatPodDetail(any(), any());
    }

    @Test
    void describePod_podFound_queriesEventsAndFormats() {
        Pod pod = new PodBuilder()
                .withNewMetadata().withName("my-pod").withNamespace("default").endMetadata()
                .build();
        List<Event> events = List.of();

        when(queries.getPod("default", "my-pod")).thenReturn(Optional.of(pod));
        when(queries.listEvents(eq("default"), eq("Pod"), eq("my-pod"), anyInt())).thenReturn(events);
        when(formatter.formatPodDetail(pod, events)).thenReturn("pod detail");

        String result = service.describePod("default", "my-pod");

        assertThat(result).isEqualTo("pod detail");
    }

    // -------------------------------------------------------------------------
    // Namespace allowlist
    // -------------------------------------------------------------------------

    @Test
    void listPods_namespaceNotInAllowlist_throwsWithoutQueryCall() {
        props.setAllowedNamespaces(List.of("default", "kube-system"));

        assertThatThrownBy(() -> service.listPods("forbidden-ns", null, null))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("allowed-namespaces");
        verify(queries, never()).listPods(anyString(), any());
    }

    @Test
    void listPods_namespaceInAllowlist_proceedsNormally() {
        props.setAllowedNamespaces(List.of("default", "kube-system"));

        List<Pod> pods = List.of();
        when(queries.listPods("default", null)).thenReturn(pods);
        when(formatter.formatPodList(pods)).thenReturn("ok");

        assertThat(service.listPods("default", null, null)).isEqualTo("ok");
    }

    @Test
    void listPods_nullNamespaceWithAllowlist_proceedsNormally() {
        props.setAllowedNamespaces(List.of("default"));

        List<Pod> pods = List.of();
        when(queries.listPods(isNull(), isNull())).thenReturn(pods);
        when(formatter.formatPodList(pods)).thenReturn("all ns");

        assertThat(service.listPods(null, null, null)).isEqualTo("all ns");
    }

    // -------------------------------------------------------------------------
    // topNodes / topPods – graceful handling of missing metrics-server
    // -------------------------------------------------------------------------

    @Test
    void topNodes_metricsServerUnavailable_returnsGracefulErrorString() {
        when(queries.topNodes()).thenThrow(new KubernetesClientException("metrics unavailable"));

        String result = service.topNodes();

        assertThat(result).contains("unavailable");
        assertThat(result).doesNotContain("Exception");
    }

    @Test
    void topPods_metricsServerUnavailable_returnsGracefulErrorString() {
        when(queries.topPods(isNull())).thenThrow(new KubernetesClientException("metrics unavailable"));

        String result = service.topPods(null);

        assertThat(result).contains("unavailable");
    }

    @Test
    void topNodes_success_formatsMetrics() {
        NodeMetricsList metrics = new NodeMetricsList();
        when(queries.topNodes()).thenReturn(metrics);
        when(formatter.formatNodeMetrics(metrics)).thenReturn("node metrics");

        assertThat(service.topNodes()).isEqualTo("node metrics");
    }

    // -------------------------------------------------------------------------
    // getEvents
    // -------------------------------------------------------------------------

    @Test
    void getEvents_withKindAndName_passesStringValueToQueries() {
        List<Event> events = List.of();
        when(queries.listEvents(eq("default"), eq("Pod"), eq("my-pod"), anyInt())).thenReturn(events);
        when(formatter.formatEventList(events)).thenReturn("events output");

        String result = service.getEvents("default", InvolvedObjectKind.Pod, "my-pod");

        assertThat(result).isEqualTo("events output");
    }

    @Test
    void getEvents_nullKindAndName_passesNullsToQueries() {
        List<Event> events = List.of();
        when(queries.listEvents(isNull(), isNull(), isNull(), anyInt())).thenReturn(events);
        when(formatter.formatEventList(events)).thenReturn("all events");

        assertThat(service.getEvents(null, null, null)).isEqualTo("all events");
    }

    @Test
    void getEvents_shellInjectionInObjectName_neverCallsQueries() {
        assertThatThrownBy(() -> service.getEvents("default", InvolvedObjectKind.Pod, "pod;cat /etc/passwd"))
                .isInstanceOf(InvalidClusterInputException.class);
        verify(queries, never()).listEvents(anyString(), anyString(), anyString(), anyInt());
    }
}
