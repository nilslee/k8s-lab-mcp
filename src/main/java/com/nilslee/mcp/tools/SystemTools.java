package com.nilslee.mcp.tools;

import com.nilslee.mcp.service.ShellScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemTools {

  private static final Logger log = LoggerFactory.getLogger(SystemTools.class);
  private final ShellScriptExecutor executor;

  private static final String context = "For K8s-Lab Cluster Diagnosis, consider this tool. Purpose: ";

  @Autowired
  public SystemTools(ShellScriptExecutor executor) {
    this.executor = executor;
  }

  @McpTool(name = "cluster-status", description = context + "Get node conditions, k3s version, Ready/NotReady counts")
  public String clusterStatus() {
    return executor.execute("./scripts/cluster-resources/cluster-status.sh", List.of());
  }

  @McpTool(name = "list-pods", description = context + "List pods by namespace with status, restarts, age. Filter by namespace, label, or status")
  public String listPods() {
    return executor.execute("./scripts/cluster-resources/list-pods.sh", List.of());
  }

  @McpTool(name = "describe-pod", description = context + "Detailed pod info: events, conditions, container statuses, resource usage")
  public String describePods() {
    return executor.execute("./scripts/cluster-resources/describe-pod.sh", List.of());
  }

  @McpTool(name = "list-deployments", description = context + "Get Deployment status: desired/ready replicas, image tags, last rollout")
  public String listDeployments() {
    return executor.execute("./scripts/cluster-resources/list-deployments.sh", List.of());
  }

  @McpTool(name = "list-services", description = context + "Get services with type, ClusterIP, ports, selectors")
  public String listServices() {
    return executor.execute("./scripts/cluster-resources/list-services.sh", List.of());
  }

  @McpTool(name = "get-events", description = context + "Get recent cluster events, filterable by namespace and involved object ")
  public String getEvents() {
    return executor.execute("./scripts/cluster-resources/get-events.sh", List.of());
  }

  @McpTool(name = "top-nodes", description = context + "Get CPU and memory usage per node")
  public String topNodes() {
    return executor.execute("./scripts/cluster-resources/top-nodes.sh", List.of());
  }

  @McpTool(name = "top-pods", description = context + "Get CPU and memory usage per pod")
  public String topPods() {
    return executor.execute("./scripts/cluster-resources/top-pods.sh", List.of());
  }

}
