# k8s-lab MCP Server

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server for the k8s-lab project. It exposes **read-only Kubernetes cluster visibility** and **Grafana Loki log access** to coding agents (for example Cursor) over **HTTP** using the Streamable transport, so assistants can inspect workloads and logs without shell access.

## Stack and transport

| Piece | Details |
| ----- | ------- |
| Framework | Spring Boot (see `pom.xml`) |
| MCP | `spring-ai-starter-mcp-server-webmvc` (Spring AI `2.0.0-M3`) |
| Kubernetes | Fabric8 `kubernetes-client` — all cluster tools use the **Kubernetes API** via Java (list/get only; no create/update/delete, exec, or port-forward) |
| Logs | Spring `RestClient` to Loki’s HTTP API (`/loki/api/v1/*`) |
| Default port | `9000` (`server.port` in `src/main/resources/application.yaml`) |

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

| Tool | Description |
| ---- | ----------- |
| `cluster-status` | Node conditions, kubelet version, Ready/NotReady counts |
| `list-namespaces` | Namespaces with name, status, and age |
| `list-pods` | Pods with status, restarts, age. Optional: `namespace` (default all), `labelSelector` (single `key=value`; comma-separated selectors not supported), `podPhaseFilter` (`ALL`, `RUNNING`, `PENDING`, `FAILED`, `SUCCEEDED`, `UNKNOWN`) |
| `describe-pod` | Pod detail: events, container statuses, resource limits. Required: `namespace`, `podName` |
| `list-deployments` | Desired/ready replicas, image tags, rollout. Optional: `namespace` (default all) |
| `list-services` | Type, ClusterIP, ports, selectors. Optional: `namespace` (default all) |
| `get-events` | Recent events; optional `namespace`, `involvedObjectKind`, `involvedObjectName` |
| `top-nodes` | CPU and memory per node (metrics API) |
| `top-pods` | CPU and memory per pod. Optional: `namespace` (default all) |

Implementation lives in `SystemTools` → `ClusterResourceService` → `Fabric8ClusterResourceQueries`.

### Logs (Loki HTTP API)

Timestamps are **Unix epoch nanoseconds** unless noted. Loki responses are returned as **JSON text** for the model (subject to `mcp.tool-response` above).

| Tool | Description |
| ---- | ----------- |
| `list-loki-labels` | Label names; optional `startNanosInclusive`, `endNanosInclusive`, `namespace` (scopes via `match={namespace="…"}`). Do not use `0` for start/end (epoch window is usually empty) |
| `list-loki-label-values` | Values for one `labelName`; optional start/end nanoseconds, optional `namespace` scope |
| `list-loki-series` | Stream label-sets; required `streamSelector`, `startNanosInclusive`, `endNanosInclusive`; optional `additionalStreamSelectors` (extra `match[]`, intersection) |
| `query-logs` | Time-range logs via `query_range`: `query`, `startNanosInclusive`, `endNanosInclusive`; optional `limit`, `direction` (`FORWARD` / `BACKWARD`) |
| `tail-logs` | Newest lines near one instant: `query`, `startNanosInclusive` (**as-of end** of window); 1h lookback, backward `query_range`. Optional `limit`, `delayForSeconds` (ingest lag). **Not** live WebSocket tailing |

Implementation lives in `LogTools` → `LokiLogService`.

### Shell scripts in this repo

The Docker image includes `scripts/cluster-resources/*.sh` and `kubectl` for **manual or scripted** use. The **Java MCP tools listed above do not invoke those scripts**; they use Fabric8 and `RestClient` only.

## Run locally

From the `mcp/` directory:

```bash
./mvnw spring-boot:run
```

Ensure a valid kubeconfig for your cluster (or set `KUBECONFIG`) and adjust `spring.http.serviceclient.loki.base-url` (or override via Spring Boot config mechanisms) so Loki is reachable. Set `GRAFANA_USERNAME` / `GRAFANA_PASSWORD` if your Loki endpoint is behind Grafana basic auth.

**Docker:** see `Dockerfile` — exposes port `9000`, runs the packaged JAR with `-Xmx384m`, user `mcp`, optional `SPRING_PROFILES_ACTIVE=runner`.

## Planned features (not implemented yet)

These match empty tool classes and commented configuration in `application.yaml`; they are **roadmap** items, not shipped tools.

| Area | Intent | Code / config hints |
| ---- | ------ | ------------------- |
| **Metrics (Prometheus)** | Raw PromQL and/or canned queries (for example pod/node/cluster resource views) | `MetricTools.java` (stub); commented `spring.http.serviceclient.prometheus` |
| **GitOps** | Argo CD application status, sync, diffs; possible Jenkins integration | `GitOpsTools.java` (stub); commented `argoCD` / `jenkins` under `spring.http.serviceclient` and `mcp.git-ops` |
| **Compound / convenience** | Single-call workflows (for example pod diagnosis combining events, logs, metrics) | `CompoundTools.java` (stub) — examples from earlier design: `diagnose-pod`, `namespace-overview` |
| **Other transports** | Today `stdio: false`; stdio or additional MCP deployment modes could be added later | `spring.ai.mcp.server` |

When implemented, this section should shrink and the tools should move into **Current tools** with accurate parameters and backing APIs.
