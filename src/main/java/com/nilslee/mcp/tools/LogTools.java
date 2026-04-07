package com.nilslee.mcp.tools;

import com.nilslee.mcp.model.logs.LogDirection;
import com.nilslee.mcp.service.logs.LokiLogService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * MCP tools backed by Loki ({@code /loki/api/v1/*}). Timestamps are Unix epoch nanoseconds unless
 * noted otherwise in each tool’s description.
 */
@Component
public class LogTools {

  private static final Logger log = LoggerFactory.getLogger(LogTools.class);

  private static final String CONTEXT =
      "For k8s-lab logs via Grafana Loki (Promtail labels: job, namespace, pod, container, …). ";

  private final LokiLogService lokiLogService;

  public LogTools(LokiLogService lokiLogService) {
    this.lokiLogService = lokiLogService;
  }

  /**
   * Exposes {@link LokiLogService#listLabels(Long, Long, String)} to MCP clients.
   *
   * @param startNanosInclusive optional range start (inclusive), nanoseconds
   * @param endNanosInclusive   optional range end (inclusive), nanoseconds
   * @param namespace           optional; when set, restricts the Loki request with {@code match={namespace="…"}}
   * @return Loki JSON response body as text for the model
   */
  @McpTool(
      name = "list-loki-labels",
      description =
          CONTEXT
              + "List log label names. Use when queries return empty or to discover dimensions. "
              + "Optional start/end are Unix nanoseconds to scope active streams; omit both (or use null) "
              + "so Loki uses its default time window. Do not use 0 for start/end—that narrows to the epoch and returns no labels. "
              + "Optional namespace scopes discovery via Loki match={namespace=\"…\"}.")
  public String listLokiLabels(
      @Nullable Long startNanosInclusive,
      @Nullable Long endNanosInclusive,
      @Nullable String namespace) {
    log.debug("list-loki-labels start={} end={} namespace={}", startNanosInclusive, endNanosInclusive, namespace);
    return lokiLogService.listLabels(startNanosInclusive, endNanosInclusive, namespace);
  }

  /**
   * Exposes {@link LokiLogService#listLabelValues(String, Long, Long, String)} to MCP clients.
   *
   * @param labelName           Loki label name (path segment, e.g. {@code namespace}, {@code pod})
   * @param startNanosInclusive optional range start (inclusive), nanoseconds
   * @param endNanosInclusive   optional range end (inclusive), nanoseconds
   * @param namespace           optional {@code match} scope for the values request
   * @return Loki JSON response body as text for the model
   */
  @McpTool(
      name = "list-loki-label-values",
      description =
          CONTEXT
              + "List values for one label (e.g. job, namespace, pod). "
              + "Optional start/end in Unix nanoseconds narrow the window; omit or null for Loki defaults. "
              + "Do not use 0 for start/end (epoch window, usually empty). "
              + "Optional namespace scopes discovery via Loki match={namespace=\"…\"}.")
  public String listLokiLabelValues(
      String labelName,
      @Nullable Long startNanosInclusive,
      @Nullable Long endNanosInclusive,
      @Nullable String namespace) {
    log.debug(
        "list-loki-label-values label={} start={} end={} namespace={}",
        labelName,
        startNanosInclusive,
        endNanosInclusive,
        namespace);
    return lokiLogService.listLabelValues(labelName, startNanosInclusive, endNanosInclusive, namespace);
  }

  /**
   * Exposes {@link LokiLogService#listSeries(String, long, long, java.util.List)} to MCP clients.
   *
   * @param streamSelector            primary LogQL stream selector (first {@code match[]} parameter)
   * @param startNanosInclusive       range start (inclusive), nanoseconds
   * @param endNanosInclusive         range end (inclusive), nanoseconds
   * @param additionalStreamSelectors optional extra selectors; each becomes another {@code match[]} (intersection)
   * @return Loki JSON response body as text for the model
   */
  @McpTool(
      name = "list-loki-series",
      description =
          CONTEXT
              + "List stream label-sets matching stream selector(s) between start and end (Unix ns). "
              + "First selector is required; optional additionalStreamSelectors are extra Loki match[] "
              + "strings (intersection).")
  public String listLokiSeries(
      String streamSelector,
      long startNanosInclusive,
      long endNanosInclusive,
      @Nullable ArrayList<String> additionalStreamSelectors) {
    log.debug(
        "list-loki-series match={} start={} end={} additionalMatches={}",
        streamSelector,
        startNanosInclusive,
        endNanosInclusive,
        additionalStreamSelectors);
    return lokiLogService.listSeries(
        streamSelector, startNanosInclusive, endNanosInclusive, additionalStreamSelectors);
  }

  /**
   * Exposes {@link LokiLogService#queryLogs(String, long, long, Integer, LogDirection)} to MCP clients
   * ({@code GET /loki/api/v1/query_range}).
   *
   * @param query               full LogQL (stream selector and optional pipeline)
   * @param startNanosInclusive range start (inclusive), nanoseconds
   * @param endNanosInclusive   range end (inclusive), nanoseconds
   * @param limit               optional max lines; {@code null} uses Loki default
   * @param direction           {@link LogDirection#FORWARD} or {@link LogDirection#BACKWARD}; {@code null} defaults to backward
   * @return Loki JSON response body as text for the model
   */
  @McpTool(
      name = "query-logs",
      description =
          CONTEXT
              + "Fetch logs between start and end (Unix nanoseconds). "
              + "Optional limit and direction (FORWARD/BACKWARD). "
              + "If empty, use list-loki-labels / list-loki-label-values / list-loki-series.")
  public String queryLogs(
      String query,
      long startNanosInclusive,
      long endNanosInclusive,
      @Nullable Integer limit,
      @Nullable LogDirection direction) {
    log.debug("query-logs query={} start={} end={}", query, startNanosInclusive, endNanosInclusive);
    return lokiLogService.queryLogs(query, startNanosInclusive, endNanosInclusive, limit, direction);
  }

  /**
   * Exposes {@link LokiLogService#tailLogs(String, long, Integer, Integer)} to MCP clients
   * ({@code GET /loki/api/v1/query_range} with a fixed lookback ending at the given instant; not live tail).
   *
   * @param query               full LogQL
   * @param startNanosInclusive As-of end of the tail window (Unix ns); logs are fetched backward from this instant
   * @param limit               optional max entries
   * @param delayForSeconds     optional ingest lag: subtract this many seconds from the as-of instant before querying
   * @return Loki JSON response body as text for the model
   */
  @McpTool(
      name = "tail-logs",
      description =
          CONTEXT
              + "Newest log lines near one instant: Unix ns `startNanosInclusive` is the range end (as-of time); "
              + "uses query_range with a 1h lookback and backward direction (Loki disallows log selectors on "
              + "instant /query). Optional delayForSeconds subtracts ingest lag from that end. Not WebSocket tail.")
  public String tailLogs(
      String query,
      long startNanosInclusive,
      @Nullable Integer limit,
      @Nullable Integer delayForSeconds) {
    log.debug(
        "tail-logs query={} asOfEndNanos={} limit={} delayForSeconds={}",
        query,
        startNanosInclusive,
        limit,
        delayForSeconds);
    return lokiLogService.tailLogs(query, startNanosInclusive, limit, delayForSeconds);
  }
}
