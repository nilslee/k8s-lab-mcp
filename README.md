# k8s-lab MCP Server

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server for the k8s-lab project. It exposes **read-only Kubernetes cluster visibility** and **Grafana Loki log access** to coding agents (for example Cursor) over **HTTP** using the Streamable transport, so assistants can inspect workloads and logs without shell access.

## Stack and transport

| Piece        | Details                                                                                                                                             |
| ------------ | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| Framework    | Spring Boot (see `pom.xml`)                                                                                                                         |
| MCP          | `spring-ai-starter-mcp-server-webmvc` (Spring AI `2.0.0-M3`)                                                                                        |
| Kubernetes   | Fabric8 `kubernetes-client` — all cluster tools use the **Kubernetes API** via Java (list/get only; no create/update/delete, exec, or port-forward) |
| Logs         | Spring `RestClient` to Loki’s HTTP API (`/loki/api/v1/*`)                                                                                           |
| Default port | `9000` (`server.port` in `src/main/resources/application.yaml`)                                                                                     |

MCP server metadata in `application.yaml`: `spring.ai.mcp.server` — `protocol: streamable`, `stdio: false`, `type: sync`, `version: 0.1.1`.

Spring Boot Actuator exposes `health` and `info` under the management web base path (defaults apply).

## Configuration

**Loki HTTP client** (`spring.http.serviceclient.loki`):

- `base-url` — default `http://loki.k8s.lab`
- `connect-timeout`, `read-timeout` — defaults `10s` / `30s`

**Optional Grafana basic auth** for Loki (`mcp.monitoring.loki`):

- `username` / `password` — often bound from env `GRAFANA_USERNAME` and `GRAFANA_PASSWORD` (see `application.yaml`)

**Kubernetes client** (Fabric8 uses standard kubeconfig discovery unless overridden):

- In Docker (`Dockerfile`), `KUBECONFIG=/app/kubeconfig` is set for a bind-mounted kubeconfig.

**MCP Kubernetes tuning** (`mcp.kubernetes`):

- `max-events` — cap for `get-events` (default `500`)
- `connection-timeout`, `read-timeout` — API server timeouts (defaults `10s` / `30s`)

**Loki JSON responses and MCP clients** (`mcp.tool-response`):

- `serialize-top-level-data-json-array` (default `true`) — when tool output is JSON with a top-level `data` array, it can be rewritten to a JSON string so some MCP clients preserve the payload. Set to `false` if you need raw Loki-style arrays.

## Current tools (implemented)

### Cluster (Kubernetes API via Fabric8)

`top-nodes` and `top-pods` require **metrics-server** in the cluster; otherwise the tools return a clear error string.

| Tool               | Description                                                                                                                                                                                                                           |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `cluster-status`   | Node conditions, kubelet version, Ready/NotReady counts                                                                                                                                                                               |
| `list-namespaces`  | Namespaces with name, status, and age                                                                                                                                                                                                 |
| `list-pods`        | Pods with status, restarts, age. Optional: `namespace` (default all), `labelSelector` (single `key=value`; comma-separated selectors not supported), `podPhaseFilter` (`ALL`, `RUNNING`, `PENDING`, `FAILED`, `SUCCEEDED`, `UNKNOWN`) |
| `describe-pod`     | Pod detail: events, container statuses, resource limits. Required: `namespace`, `podName`                                                                                                                                             |
| `list-deployments` | Desired/ready replicas, image tags, rollout. Optional: `namespace` (default all)                                                                                                                                                      |
| `list-services`    | Type, ClusterIP, ports, selectors. Optional: `namespace` (default all)                                                                                                                                                                |
| `get-events`       | Recent events; optional `namespace`, `involvedObjectKind`, `involvedObjectName`                                                                                                                                                       |
| `top-nodes`        | CPU and memory per node (metrics API)                                                                                                                                                                                                 |
| `top-pods`         | CPU and memory per pod. Optional: `namespace` (default all)                                                                                                                                                                           |

Implementation lives in `SystemTools` → `ClusterResourceService` → `Fabric8ClusterResourceQueries`.

### Logs (Loki HTTP API)

Timestamps are **Unix epoch nanoseconds** unless noted. Loki responses are returned as **JSON text** for the model (subject to `mcp.tool-response` above).

| Tool                     | Description                                                                                                                                                                                                    |
| ------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `list-loki-labels`       | Label names; optional `startNanosInclusive`, `endNanosInclusive`, `namespace` (scopes via `match={namespace="…"}`). Do not use `0` for start/end (epoch window is usually empty)                               |
| `list-loki-label-values` | Values for one `labelName`; optional start/end nanoseconds, optional `namespace` scope                                                                                                                         |
| `list-loki-series`       | Stream label-sets; required `streamSelector`, `startNanosInclusive`, `endNanosInclusive`; optional `additionalStreamSelectors` (extra `match[]`, intersection)                                                 |
| `query-logs`             | Time-range logs via `query_range`: `query`, `startNanosInclusive`, `endNanosInclusive`; optional `limit`, `direction` (`FORWARD` / `BACKWARD`)                                                                 |
| `tail-logs`              | Newest lines near one instant: `query`, `startNanosInclusive` (**as-of end** of window); 1h lookback, backward `query_range`. Optional `limit`, `delayForSeconds` (ingest lag). **Not** live WebSocket tailing |

Implementation lives in `LogTools` → `LokiLogService`.

## Run in cluster

**Cluster setup:** Provision the K8s-Lab platform (multi-node K3s, monitoring, GitOps, and related wiring) from **[nilslee/cluster-infra](https://github.com/nilslee/cluster-infra/)**. That repository is the source of truth for VMs, k3s, Argo CD, Jenkins, and the observability stack; deploy it before running this MCP server against the lab.

**Runtime expectations:** The server is intended to run **alongside** the cluster (for example on a Jenkins/build host or another VM that can reach both the Kubernetes API and Loki), not necessarily as a Pod inside the cluster—though you could deploy it in-cluster if you mount credentials and point `spring.http.serviceclient.loki.base-url` at a resolvable in-cluster Loki/Grafana URL.

1. **Build the image** from this directory (see `Dockerfile`):

   ```bash
   cd mcp
   docker build -t mcp-server:latest .
   ```

2. **Run the container** with a **read-only kubeconfig** mounted where the image expects it (`KUBECONFIG=/app/kubeconfig`):

   ```bash
   docker run -d \
     --name mcp-server \
     --restart=unless-stopped \
     --memory=512m --memory-swap=512m \
     --network=host \
     -e SPRING_PROFILES_ACTIVE=runner \
     -e GRAFANA_USERNAME="..." \
     -e GRAFANA_PASSWORD="..." \
     -v /path/to/kubeconfig:/app/kubeconfig:ro \
     mcp-server:latest
   ```

   Use `--network=host` (or another network mode) so hostnames such as `loki.k8s.lab` from `application.yaml` resolve the same way as on your lab machines. Omit or adjust Grafana env vars if Loki is open on your network.

3. **Verify:** Actuator health and MCP HTTP endpoint (typical paths):

   - `http://<host>:9000/actuator/health`
   - `http://<host>:9000/mcp`

**CI example:** The lab Jenkins pipeline at [`cluster-infra/jenkins/pipelines/mcp-server.Jenkinsfile`](https://github.com/nilslee/cluster-infra/blob/main/jenkins/pipelines/mcp-server.Jenkinsfile) in [nilslee/cluster-infra](https://github.com/nilslee/cluster-infra/) builds the image, runs the container with a mounted kubeconfig and Grafana credentials, and checks `/actuator/health`. Point your MCP client at the same host and port (`9000` by default).

**Image details:** `Dockerfile` packages the JAR with `-Xmx384m`, user `mcp`, exposes port `9000`.

## Planned features (not implemented yet)

These match empty tool classes and commented configuration in `application.yaml`; they are **roadmap** items, not shipped tools.

| Area                       | Intent                                                                              | Code / config hints                                                                                           |
| -------------------------- | ----------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **Metrics (Prometheus)**   | Raw PromQL and/or canned queries (for example pod/node/cluster resource views)      | `MetricTools.java` (stub); commented `spring.http.serviceclient.prometheus`                                   |
| **GitOps**                 | Argo CD application status, sync, diffs; possible Jenkins integration               | `GitOpsTools.java` (stub); commented `argoCD` / `jenkins` under `spring.http.serviceclient` and `mcp.git-ops` |
| **Compound / convenience** | Single-call workflows (for example pod diagnosis combining events, logs, metrics)   | `CompoundTools.java` (stub) — examples from earlier design: `diagnose-pod`, `namespace-overview`              |
| **Other transports**       | Today `stdio: false`; stdio or additional MCP deployment modes could be added later | `spring.ai.mcp.server`                                                                                        |

When implemented, this section should shrink and the tools should move into **Current tools** with accurate parameters and backing APIs.
