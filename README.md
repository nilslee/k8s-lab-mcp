# MCP Server Tools

### Tier 1 -- Cluster Health and Resources (via Kubernetes API)

| Tool               | Description                                                                              | Backing API                                   |
| ------------------ | ---------------------------------------------------------------------------------------- | --------------------------------------------- |
| `cluster-status`   | Node conditions, k3s version, Ready/NotReady counts                                      | `kubectl get nodes`                           |
| `list-pods`        | List pods by namespace with status, restarts, age. Filter by namespace, label, or status | `kubectl get pods`                            |
| `describe-pod`     | Detailed pod info: events, conditions, container statuses, resource usage                | `kubectl describe pod`                        |
| `list-deployments` | Deployment status: desired/ready replicas, image tags, last rollout                      | `kubectl get deployments`                     |
| `list-services`    | Services with type, ClusterIP, ports, selectors                                          | `kubectl get services`                        |
| `get-events`       | Recent cluster events, filterable by namespace and involved object                       | `kubectl get events --sort-by=.lastTimestamp` |
| `top-nodes`        | CPU and memory usage per node                                                            | `kubectl top nodes`                           |
| `top-pods`         | CPU and memory usage per pod                                                             | `kubectl top pods`                            |

### Tier 2 -- Logs (via Loki API)

| Tool         | Description                                                                                                                   | Backing API                           |
| ------------ | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| `query-logs` | Fetch logs by pod, namespace, container, or label selector with time range. Accepts LogQL or builds it from structured params | Loki `GET /loki/api/v1/query_range`   |
| `tail-logs`  | Retrieve the most recent N log lines for a pod/container                                                                      | Loki `GET /loki/api/v1/query` (limit) |

### Tier 3 -- Metrics (via Prometheus API)

| Tool                       | Description                                                           | Backing API                                                                       |
| -------------------------- | --------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| `query-metrics`            | Execute a raw PromQL query (instant or range)                         | Prometheus `GET /api/v1/query` or `/query_range`                                  |
| `pod-resource-usage`       | Pre-built query: CPU and memory for a specific pod over a time window | PromQL: `container_cpu_usage_seconds_total`, `container_memory_working_set_bytes` |
| `node-resource-usage`      | Pre-built query: CPU, memory, disk for a specific node                | PromQL: `node_cpu_seconds_total`, `node_memory_MemAvailable_bytes`, etc.          |
| `cluster-resource-summary` | Pre-built query: cluster-wide CPU/memory requests vs. capacity        | PromQL aggregate queries                                                          |

### Tier 4 -- GitOps (via ArgoCD API)

| Tool                | Description                                                                      | Backing API                                             |
| ------------------- | -------------------------------------------------------------------------------- | ------------------------------------------------------- |
| `argocd-app-status` | Sync status, health, and last sync time for all or a specific ArgoCD Application | ArgoCD REST API `/api/v1/applications`                  |
| `argocd-sync`       | Trigger a sync for a specific application                                        | ArgoCD REST API `POST /api/v1/applications/{name}/sync` |
| `argocd-diff`       | Show the diff between desired (Git) and live state                               | ArgoCD REST API `/api/v1/applications/{name}/manifests` |

### Tier 5 -- Compound / Convenience

| Tool                 | Description                                                                                                         |
| -------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `diagnose-pod`       | Given a pod name, return: status, events, recent logs (from Loki), and resource usage (from Prometheus) in one call |
| `namespace-overview` | For a namespace: all deployments, pods, services, recent events, and resource totals                                |
