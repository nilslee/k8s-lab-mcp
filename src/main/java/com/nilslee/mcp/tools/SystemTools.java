package com.nilslee.mcp.tools;

import com.nilslee.mcp.service.cluster.ClusterResourceService;
import com.nilslee.mcp.model.cluster.InvolvedObjectKind;
import com.nilslee.mcp.model.cluster.PodPhaseFilter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class SystemTools {

    private static final Logger log = LoggerFactory.getLogger(SystemTools.class);

    private static final String CONTEXT = "For K8s-Lab Cluster Diagnosis. ";

    private final ClusterResourceService clusterResourceService;

    public SystemTools(ClusterResourceService clusterResourceService) {
        this.clusterResourceService = clusterResourceService;
    }

    /**
     * MCP tool: cluster-wide node health summary.
     *
     * @return formatted node list and status text
     */
    @McpTool(name = "cluster-status",
            description = CONTEXT + "Get node conditions, kubelet version, Ready/NotReady counts.")
    public String clusterStatus() {
        return clusterResourceService.clusterStatus();
    }

    /**
     * MCP tool: namespaces in the cluster.
     *
     * @return formatted namespace list
     */
    @McpTool(name = "list-namespaces",
            description = CONTEXT + "List namespaces with name, status, and age.")
    public String listNamespaces() {
        return clusterResourceService.listNamespaces();
    }

    /**
     * MCP tool: pod inventory with optional filters (k8s-lab allow-list may apply).
     *
     * @param namespace       {@code null} or blank = all namespaces
     * @param labelSelector   {@code null} or single {@code key=value}; comma-separated selectors unsupported
     * @param podPhaseFilter  {@code null} or {@link PodPhaseFilter#ALL} = all phases
     * @return formatted pod table
     */
    @McpTool(name = "list-pods",
            description = CONTEXT + "List pods with status, restarts, and age. "
                    + "Optional: namespace (default=all), labelSelector (key=value), "
                    + "podPhaseFilter (ALL|RUNNING|PENDING|FAILED|SUCCEEDED|UNKNOWN).")
    public String listPods(@Nullable String namespace,
                           @Nullable String labelSelector,
                           @Nullable PodPhaseFilter podPhaseFilter) {
        log.debug("list-pods ns={} selector={} phase={}", namespace, labelSelector, podPhaseFilter);
        return clusterResourceService.listPods(namespace, labelSelector, podPhaseFilter);
    }

    /**
     * MCP tool: deep dive on one pod (events, containers, resources).
     *
     * @param namespace Kubernetes namespace
     * @param podName   pod name
     * @return formatted detail or not-found message
     */
    @McpTool(name = "describe-pod",
            description = CONTEXT + "Detailed pod info including events, container statuses, and resource limits. "
                    + "Required: namespace, podName.")
    public String describePod(String namespace, String podName) {
        log.debug("describe-pod ns={} pod={}", namespace, podName);
        return clusterResourceService.describePod(namespace, podName);
    }

    /**
     * MCP tool: deployment rollout and image summary.
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted deployment list
     */
    @McpTool(name = "list-deployments",
            description = CONTEXT + "Get Deployment status: desired/ready replicas, image tags, last rollout. "
                    + "Optional: namespace (default=all).")
    public String listDeployments(@Nullable String namespace) {
        log.debug("list-deployments ns={}", namespace);
        return clusterResourceService.listDeployments(namespace);
    }

    /**
     * MCP tool: ClusterIP/NodePort/LoadBalancer-style service summary.
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted service list
     */
    @McpTool(name = "list-services",
            description = CONTEXT + "Get services with type, ClusterIP, ports, selectors. "
                    + "Optional: namespace (default=all).")
    public String listServices(@Nullable String namespace) {
        log.debug("list-services ns={}", namespace);
        return clusterResourceService.listServices(namespace);
    }

    /**
     * MCP tool: Kubernetes events (volume capped by {@code mcp.kubernetes.max-events}).
     *
     * @param namespace          {@code null} or blank = all namespaces
     * @param involvedObjectKind {@code null} = any kind
     * @param involvedObjectName {@code null} or blank = filter by kind only
     * @return formatted event list
     */
    @McpTool(name = "get-events",
            description = CONTEXT + "Get recent cluster events. "
                    + "Optional: namespace (default=all), involvedObjectKind "
                    + "(Pod|Deployment|ReplicaSet|StatefulSet|DaemonSet|Job|CronJob|Service|Node|...), "
                    + "involvedObjectName.")
    public String getEvents(@Nullable String namespace,
                            @Nullable InvolvedObjectKind involvedObjectKind,
                            @Nullable String involvedObjectName) {
        log.debug("get-events ns={} kind={} name={}", namespace, involvedObjectKind, involvedObjectName);
        return clusterResourceService.getEvents(namespace, involvedObjectKind, involvedObjectName);
    }

    /**
     * MCP tool: node CPU/memory from metrics-server.
     *
     * @return formatted metrics or graceful error if metrics-server is missing
     */
    @McpTool(name = "top-nodes",
            description = CONTEXT + "Get CPU and memory usage per node (requires metrics-server).")
    public String topNodes() {
        return clusterResourceService.topNodes();
    }

    /**
     * MCP tool: pod CPU/memory from metrics-server.
     *
     * @param namespace {@code null} or blank = all namespaces
     * @return formatted metrics or graceful error if metrics-server is missing
     */
    @McpTool(name = "top-pods",
            description = CONTEXT + "Get CPU and memory usage per pod (requires metrics-server). "
                    + "Optional: namespace (default=all).")
    public String topPods(@Nullable String namespace) {
        log.debug("top-pods ns={}", namespace);
        return clusterResourceService.topPods(namespace);
    }
}
