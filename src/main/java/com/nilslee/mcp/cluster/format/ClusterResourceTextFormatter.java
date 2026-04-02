package com.nilslee.mcp.cluster.format;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetricsList;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetrics;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.PodMetricsList;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Produces stable, human-readable text for each MCP tool response.
 * All formatting is centralised here to avoid duplicating string building across tools.
 */
@Component
public class ClusterResourceTextFormatter {

    // -------------------------------------------------------------------------
    // Nodes / cluster-status
    // -------------------------------------------------------------------------

    public String formatNodeList(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return "No nodes found.";
        }
        long ready = nodes.stream()
                .filter(n -> isNodeReady(n))
                .count();
        long notReady = nodes.size() - ready;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Nodes: %d  (Ready: %d, NotReady: %d)%n%n", nodes.size(), ready, notReady));
        sb.append(String.format("%-30s %-10s %-20s %-10s%n", "NAME", "STATUS", "VERSION", "AGE"));
        sb.append("-".repeat(75)).append("\n");
        for (Node node : nodes) {
            String name = node.getMetadata().getName();
            String status = isNodeReady(node) ? "Ready" : "NotReady";
            String version = node.getStatus().getNodeInfo() != null
                    ? node.getStatus().getNodeInfo().getKubeletVersion() : "<unknown>";
            String age = formatAge(node.getMetadata());
            sb.append(String.format("%-30s %-10s %-20s %-10s%n", name, status, version, age));
        }
        sb.append("\nConditions:\n");
        for (Node node : nodes) {
            sb.append("  ").append(node.getMetadata().getName()).append(": ");
            List<NodeCondition> conditions = node.getStatus().getConditions();
            if (conditions != null) {
                String condStr = conditions.stream()
                        .map(c -> c.getType() + "=" + c.getStatus())
                        .collect(Collectors.joining(", "));
                sb.append("[").append(condStr).append("]");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Namespaces
    // -------------------------------------------------------------------------

    public String formatNamespaceList(List<Namespace> namespaces) {
        if (namespaces.isEmpty()) {
            return "No namespaces found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %-10s %-10s%n", "NAME", "STATUS", "AGE"));
        sb.append("-".repeat(55)).append("\n");
        for (Namespace ns : namespaces) {
            String name = ns.getMetadata().getName();
            String phase = ns.getStatus() != null && ns.getStatus().getPhase() != null
                    ? ns.getStatus().getPhase() : "<unknown>";
            String age = formatAge(ns.getMetadata());
            sb.append(String.format("%-30s %-10s %-10s%n", name, phase, age));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Pods
    // -------------------------------------------------------------------------

    public String formatPodList(List<Pod> pods) {
        if (pods.isEmpty()) {
            return "No pods found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-40s %-8s %-12s %-10s %-10s%n",
                "NAMESPACE", "NAME", "READY", "STATUS", "RESTARTS", "AGE"));
        sb.append("-".repeat(105)).append("\n");
        for (Pod pod : pods) {
            String ns = pod.getMetadata().getNamespace();
            String name = pod.getMetadata().getName();
            String phase = pod.getStatus() != null && pod.getStatus().getPhase() != null
                    ? pod.getStatus().getPhase() : "<unknown>";
            int totalContainers = pod.getSpec() != null && pod.getSpec().getContainers() != null
                    ? pod.getSpec().getContainers().size() : 0;
            long readyContainers = 0;
            int restarts = 0;
            if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
                for (ContainerStatus cs : pod.getStatus().getContainerStatuses()) {
                    if (Boolean.TRUE.equals(cs.getReady())) readyContainers++;
                    restarts += cs.getRestartCount() != null ? cs.getRestartCount() : 0;
                }
            }
            String ready = readyContainers + "/" + totalContainers;
            String age = formatAge(pod.getMetadata());
            sb.append(String.format("%-20s %-40s %-8s %-12s %-10d %-10s%n",
                    ns, name, ready, phase, restarts, age));
        }
        return sb.toString();
    }

    public String formatPodDetail(Pod pod, List<Event> events) {
        StringBuilder sb = new StringBuilder();
        ObjectMeta meta = pod.getMetadata();
        sb.append("Name:        ").append(meta.getName()).append("\n");
        sb.append("Namespace:   ").append(meta.getNamespace()).append("\n");
        sb.append("Node:        ")
                .append(pod.getSpec() != null ? pod.getSpec().getNodeName() : "<none>").append("\n");
        String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "<unknown>";
        sb.append("Status:      ").append(phase).append("\n");
        String podIp = pod.getStatus() != null ? pod.getStatus().getPodIP() : null;
        sb.append("Pod IP:      ").append(podIp != null ? podIp : "<none>").append("\n");
        sb.append("Created:     ").append(meta.getCreationTimestamp()).append("\n");

        if (meta.getLabels() != null && !meta.getLabels().isEmpty()) {
            sb.append("Labels:      ");
            sb.append(meta.getLabels().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", ")));
            sb.append("\n");
        }

        if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
            sb.append("\nContainers:\n");
            for (Container c : pod.getSpec().getContainers()) {
                sb.append("  ").append(c.getName()).append(":\n");
                sb.append("    Image:   ").append(c.getImage()).append("\n");
                if (c.getResources() != null) {
                    if (c.getResources().getRequests() != null) {
                        sb.append("    Requests: ").append(formatResourceMap(c.getResources().getRequests())).append("\n");
                    }
                    if (c.getResources().getLimits() != null) {
                        sb.append("    Limits:   ").append(formatResourceMap(c.getResources().getLimits())).append("\n");
                    }
                }
            }
        }

        if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
            sb.append("\nContainer Statuses:\n");
            for (ContainerStatus cs : pod.getStatus().getContainerStatuses()) {
                sb.append("  ").append(cs.getName()).append(": ");
                sb.append("ready=").append(cs.getReady()).append(", ");
                sb.append("restarts=").append(cs.getRestartCount()).append(", ");
                sb.append("image=").append(cs.getImage()).append("\n");
            }
        }

        if (!events.isEmpty()) {
            sb.append("\nEvents:\n");
            sb.append(String.format("  %-8s %-20s %-8s %-15s %s%n",
                    "TYPE", "REASON", "AGE", "FROM", "MESSAGE"));
            sb.append("  ").append("-".repeat(80)).append("\n");
            for (Event ev : events) {
                String evType = ev.getType() != null ? ev.getType() : "";
                String reason = ev.getReason() != null ? ev.getReason() : "";
                String age = ev.getLastTimestamp() != null ? formatRelativeTime(ev.getLastTimestamp()) : "<unknown>";
                String from = ev.getSource() != null && ev.getSource().getComponent() != null
                        ? ev.getSource().getComponent() : "";
                String msg = ev.getMessage() != null ? ev.getMessage() : "";
                if (msg.length() > 80) msg = msg.substring(0, 77) + "...";
                sb.append(String.format("  %-8s %-20s %-8s %-15s %s%n", evType, reason, age, from, msg));
            }
        } else {
            sb.append("\nEvents: <none>\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Deployments
    // -------------------------------------------------------------------------

    public String formatDeploymentList(List<Deployment> deployments) {
        if (deployments.isEmpty()) {
            return "No deployments found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-35s %-8s %-12s %-12s %-10s%n",
                "NAMESPACE", "NAME", "READY", "UP-TO-DATE", "AVAILABLE", "AGE"));
        sb.append("-".repeat(100)).append("\n");
        for (Deployment d : deployments) {
            String ns = d.getMetadata().getNamespace();
            String name = d.getMetadata().getName();
            int desired = d.getSpec() != null && d.getSpec().getReplicas() != null
                    ? d.getSpec().getReplicas() : 0;
            int ready = d.getStatus() != null && d.getStatus().getReadyReplicas() != null
                    ? d.getStatus().getReadyReplicas() : 0;
            int upToDate = d.getStatus() != null && d.getStatus().getUpdatedReplicas() != null
                    ? d.getStatus().getUpdatedReplicas() : 0;
            int available = d.getStatus() != null && d.getStatus().getAvailableReplicas() != null
                    ? d.getStatus().getAvailableReplicas() : 0;
            String age = formatAge(d.getMetadata());
            sb.append(String.format("%-20s %-35s %-8s %-12d %-12d %-10s%n",
                    ns, name, ready + "/" + desired, upToDate, available, age));
            if (d.getSpec() != null && d.getSpec().getTemplate() != null
                    && d.getSpec().getTemplate().getSpec() != null
                    && d.getSpec().getTemplate().getSpec().getContainers() != null) {
                for (Container c : d.getSpec().getTemplate().getSpec().getContainers()) {
                    sb.append(String.format("  Image: %s%n", c.getImage()));
                }
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Services
    // -------------------------------------------------------------------------

    public String formatServiceList(List<Service> services) {
        if (services.isEmpty()) {
            return "No services found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-35s %-12s %-16s %-20s %-10s%n",
                "NAMESPACE", "NAME", "TYPE", "CLUSTER-IP", "PORT(S)", "AGE"));
        sb.append("-".repeat(115)).append("\n");
        for (Service svc : services) {
            String ns = svc.getMetadata().getNamespace();
            String name = svc.getMetadata().getName();
            String type = svc.getSpec() != null && svc.getSpec().getType() != null
                    ? svc.getSpec().getType() : "<unknown>";
            String clusterIp = svc.getSpec() != null && svc.getSpec().getClusterIP() != null
                    ? svc.getSpec().getClusterIP() : "<none>";
            String ports = "<none>";
            if (svc.getSpec() != null && svc.getSpec().getPorts() != null) {
                ports = svc.getSpec().getPorts().stream()
                        .map(this::formatPort)
                        .collect(Collectors.joining(","));
            }
            String age = formatAge(svc.getMetadata());
            sb.append(String.format("%-20s %-35s %-12s %-16s %-20s %-10s%n",
                    ns, name, type, clusterIp, ports, age));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Events
    // -------------------------------------------------------------------------

    public String formatEventList(List<Event> events) {
        if (events.isEmpty()) {
            return "No events found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-10s %-8s %-20s %-35s %s%n",
                "NAMESPACE", "LAST SEEN", "TYPE", "REASON", "OBJECT", "MESSAGE"));
        sb.append("-".repeat(120)).append("\n");
        for (Event ev : events) {
            String ns = ev.getMetadata() != null ? ev.getMetadata().getNamespace() : "";
            String lastSeen = ev.getLastTimestamp() != null ? formatRelativeTime(ev.getLastTimestamp()) : "<unknown>";
            String type = ev.getType() != null ? ev.getType() : "";
            String reason = ev.getReason() != null ? ev.getReason() : "";
            String object = "";
            if (ev.getInvolvedObject() != null) {
                object = ev.getInvolvedObject().getKind() + "/" + ev.getInvolvedObject().getName();
            }
            String msg = ev.getMessage() != null ? ev.getMessage() : "";
            if (msg.length() > 60) msg = msg.substring(0, 57) + "...";
            sb.append(String.format("%-20s %-10s %-8s %-20s %-35s %s%n",
                    ns, lastSeen, type, reason, object, msg));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Metrics
    // -------------------------------------------------------------------------

    public String formatNodeMetrics(NodeMetricsList metrics) {
        List<NodeMetrics> items = metrics.getItems();
        if (items == null || items.isEmpty()) {
            return "No node metrics available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %-14s %-16s%n", "NAME", "CPU(cores)", "MEMORY(bytes)"));
        sb.append("-".repeat(62)).append("\n");
        for (NodeMetrics nm : items) {
            String name = nm.getMetadata().getName();
            String cpu = nm.getUsage() != null && nm.getUsage().get("cpu") != null
                    ? nm.getUsage().get("cpu").toString() : "<none>";
            String mem = nm.getUsage() != null && nm.getUsage().get("memory") != null
                    ? nm.getUsage().get("memory").toString() : "<none>";
            sb.append(String.format("%-30s %-14s %-16s%n", name, cpu, mem));
        }
        return sb.toString();
    }

    public String formatPodMetrics(PodMetricsList metrics) {
        List<PodMetrics> items = metrics.getItems();
        if (items == null || items.isEmpty()) {
            return "No pod metrics available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-40s %-14s %-16s%n",
                "NAMESPACE", "NAME", "CPU(cores)", "MEMORY(bytes)"));
        sb.append("-".repeat(92)).append("\n");
        for (PodMetrics pm : items) {
            String ns = pm.getMetadata().getNamespace();
            String name = pm.getMetadata().getName();
            String cpu = "<none>";
            String mem = "<none>";
            if (pm.getContainers() != null && !pm.getContainers().isEmpty()) {
                long cpuTotal = pm.getContainers().stream()
                        .mapToLong(c -> parseMilliCpu(c.getUsage() != null ? c.getUsage().get("cpu") : null))
                        .sum();
                long memTotal = pm.getContainers().stream()
                        .mapToLong(c -> parseKiBytes(c.getUsage() != null ? c.getUsage().get("memory") : null))
                        .sum();
                cpu = cpuTotal + "m";
                mem = formatKiBytes(memTotal);
            }
            sb.append(String.format("%-20s %-40s %-14s %-16s%n", ns, name, cpu, mem));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean isNodeReady(Node node) {
        if (node.getStatus() == null || node.getStatus().getConditions() == null) {
            return false;
        }
        return node.getStatus().getConditions().stream()
                .anyMatch(c -> "Ready".equals(c.getType()) && "True".equals(c.getStatus()));
    }

    private String formatAge(ObjectMeta meta) {
        if (meta == null || meta.getCreationTimestamp() == null) {
            return "<unknown>";
        }
        try {
            OffsetDateTime created = OffsetDateTime.parse(meta.getCreationTimestamp());
            Duration age = Duration.between(created.toInstant(), java.time.Instant.now());
            if (age.toDays() > 0) return age.toDays() + "d";
            if (age.toHours() > 0) return age.toHours() + "h";
            return age.toMinutes() + "m";
        } catch (DateTimeParseException e) {
            return meta.getCreationTimestamp();
        }
    }

    private String formatRelativeTime(String timestamp) {
        if (timestamp == null) return "<unknown>";
        try {
            OffsetDateTime t = OffsetDateTime.parse(timestamp);
            Duration ago = Duration.between(t.toInstant(), java.time.Instant.now());
            if (ago.toDays() > 0) return ago.toDays() + "d";
            if (ago.toHours() > 0) return ago.toHours() + "h";
            return ago.toMinutes() + "m";
        } catch (DateTimeParseException e) {
            return timestamp;
        }
    }

    private String formatPort(ServicePort p) {
        StringBuilder sb = new StringBuilder();
        if (p.getNodePort() != null) {
            sb.append(p.getPort()).append(":").append(p.getNodePort());
        } else {
            sb.append(p.getPort());
        }
        if (p.getProtocol() != null) {
            sb.append("/").append(p.getProtocol());
        }
        return sb.toString();
    }

    private String formatResourceMap(Map<String, io.fabric8.kubernetes.api.model.Quantity> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().toString())
                .collect(Collectors.joining(", "));
    }

    private long parseMilliCpu(io.fabric8.kubernetes.api.model.Quantity q) {
        if (q == null) return 0;
        String s = q.toString();
        if (s.endsWith("m")) {
            try { return Long.parseLong(s.substring(0, s.length() - 1)); } catch (NumberFormatException e) { return 0; }
        }
        try { return (long)(Double.parseDouble(s) * 1000); } catch (NumberFormatException e) { return 0; }
    }

    private long parseKiBytes(io.fabric8.kubernetes.api.model.Quantity q) {
        if (q == null) return 0;
        String s = q.toString();
        try {
            if (s.endsWith("Ki")) return Long.parseLong(s.substring(0, s.length() - 2));
            if (s.endsWith("Mi")) return Long.parseLong(s.substring(0, s.length() - 2)) * 1024;
            if (s.endsWith("Gi")) return Long.parseLong(s.substring(0, s.length() - 2)) * 1024 * 1024;
            return Long.parseLong(s) / 1024;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatKiBytes(long ki) {
        if (ki >= 1024 * 1024) return (ki / (1024 * 1024)) + "Gi";
        if (ki >= 1024) return (ki / 1024) + "Mi";
        return ki + "Ki";
    }
}
