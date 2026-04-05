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

| Tool                     | Description                                                                                                                                                                                                                                                                                                                                                                                                      | Backing API                                                |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| `list-loki-labels`       | Use when LogQL or log queries return no data, when the user asks what log dimensions or labels exist, or before constructing a stream selector. Lists label names known to Loki; optional `start` and `end` query parameters (Unix time in **nanoseconds**) scope the search to streams active in that window.                                                                                                  | Loki `GET /loki/api/v1/labels`                             |
| `list-loki-label-values` | Use when the user names a label (for example `job`, `namespace`, `pod`, `container`) and needs valid values, disambiguation, or autocompletion for LogQL. Requires the label name in the path. Optional `start` / `end` in **nanoseconds** narrow to that interval.                                                                                                                                                | Loki `GET /loki/api/v1/label/{name}/values`                |
| `list-loki-series`       | Use when the user has a partial LogQL **stream selector** and needs concrete stream label-sets, or to verify that a matcher matches streams in a time range. Requires a `match` parameter with a stream selector (for example `{namespace="monitoring"}`) plus `start` and `end` in **nanoseconds**.                                                                                                               | Loki `GET /loki/api/v1/series`                             |
| `query-logs`             | Use for **time-range** log retrieval: fetch log lines between two instants. Backed by `query_range`; requires `start` and `end` in **nanoseconds**, a LogQL `query` (stream selector plus optional filters), and supports `limit` and `direction` (`forward` or `backward`). Accepts full LogQL or structured params that build it (pod, namespace, container, label selector). If results are empty, use `list-loki-labels`, `list-loki-label-values`, or `list-loki-series` to correct selectors before retrying. | Loki `GET /loki/api/v1/query_range`                        |
| `tail-logs`              | Use for a **snapshot of the newest** log lines at a point in time without scanning a full range via `query_range`. Backed by the instant **`/query`** endpoint: supply `time` in **nanoseconds**, LogQL `query`, and `limit`. Not the same as live WebSocket tailing.                                                                                                                                            | Loki `GET /loki/api/v1/query`                              |

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
| `diagnose-pod`       | Given a pod name, return: status, events, recent logs (same Loki `query_range` path as `query-logs`), and resource usage (from Prometheus) in one call |
| `namespace-overview` | For a namespace: all deployments, pods, services, recent events, and resource totals                                |
