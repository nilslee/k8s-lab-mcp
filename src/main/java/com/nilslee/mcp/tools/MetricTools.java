package com.nilslee.mcp.tools;

import com.nilslee.mcp.service.metrics.PrometheusMetricService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tools backed by the Prometheus HTTP API v1 ({@code /api/v1/*}). Responses are Prometheus JSON
 * text for the model (status, data, errors). Timestamps use RFC3339 strings or Unix epoch seconds,
 * matching Prometheus query parameters ({@code time}, {@code start}, {@code end}).
 */
@Component
public class MetricTools {

  private static final Logger log = LoggerFactory.getLogger(MetricTools.class);

  private static final String CONTEXT =
      "Prometheus metrics API (kube-prometheus-stack or compatible). ";

  private final PrometheusMetricService prometheusMetricService;

  public MetricTools(PrometheusMetricService prometheusMetricService) {
    this.prometheusMetricService = prometheusMetricService;
  }

  /**
   * Exposes {@link PrometheusMetricService#query(String, Integer, String, String, String, String)}.
   *
   * @param query         required PromQL
   * @param limit         optional max metrics
   * @param time          optional instant (RFC3339 or Unix seconds); omit for “now”
   * @param timeout       optional (e.g. 30s)
   * @param lookbackDelta optional lookback override for range selectors in the expression
   * @param stats         optional; {@code all} for query statistics
   * @return Prometheus JSON body as text
   */
  @McpTool(
      name = "prometheus-query",
      description =
          CONTEXT
              + "Evaluate an instant PromQL query (GET /api/v1/query). "
              + "Required: query. Optional: limit, time (RFC3339 or Unix seconds), timeout, lookback_delta, stats (e.g. all).")
  public String prometheusQuery(
      String query,
      @Nullable Integer limit,
      @Nullable String time,
      @Nullable String timeout,
      @Nullable String lookbackDelta,
      @Nullable String stats) {
    log.debug(
        "prometheus-query query={} limit={} time={} timeout={} lookbackDelta={} stats={}",
        query,
        limit,
        time,
        timeout,
        lookbackDelta,
        stats);
    return prometheusMetricService.query(query, limit, time, timeout, lookbackDelta, stats);
  }

  /**
   * Exposes {@link PrometheusMetricService#queryRange(String, String, String, String, Integer, String, String, String)}.
   *
   * @param query         required PromQL
   * @param start         range start (RFC3339 or Unix seconds)
   * @param end           range end (RFC3339 or Unix seconds)
   * @param step            resolution step (duration or seconds)
   * @param limit         optional max series
   * @param timeout       optional
   * @param lookbackDelta optional
   * @param stats         optional
   * @return Prometheus JSON body as text
   */
  @McpTool(
      name = "prometheus-query-range",
      description =
          CONTEXT
              + "Evaluate a range PromQL query (GET /api/v1/query_range). "
              + "Required: query, start, end, step. Optional: limit, timeout, lookback_delta, stats.")
  public String prometheusQueryRange(
      String query,
      String start,
      String end,
      String step,
      @Nullable Integer limit,
      @Nullable String timeout,
      @Nullable String lookbackDelta,
      @Nullable String stats) {
    log.debug(
        "prometheus-query-range query={} start={} end={} step={}",
        query,
        start,
        end,
        step);
    return prometheusMetricService.queryRange(
        query, start, end, step, limit, timeout, lookbackDelta, stats);
  }

  /**
   * Exposes {@link PrometheusMetricService#listLabels(List, String, String, Integer)}.
   *
   * @param matches optional series selectors as repeated match[] (e.g. job="prometheus")
   * @param start   optional range start
   * @param end     optional range end
   * @param limit   optional max label names
   * @return JSON text
   */
  @McpTool(
      name = "list-prometheus-labels",
      description =
          CONTEXT
              + "List label names present in TSDB (GET /api/v1/labels). "
              + "Optional: matches (label selectors, match[]), start, end (RFC3339 or Unix seconds), limit.")
  public String listPrometheusLabels(
      @Nullable List<String> matches,
      @Nullable String start,
      @Nullable String end,
      @Nullable Integer limit) {
    log.debug("list-prometheus-labels matches={} start={} end={} limit={}", matches, start, end, limit);
    return prometheusMetricService.listLabels(matches, start, end, limit);
  }

  /**
   * Exposes {@link PrometheusMetricService#listLabelValues(String, String, String, List, Integer)}.
   *
   * @param name    label name
   * @param start   optional
   * @param end     optional
   * @param matches optional match[]
   * @param limit   optional
   * @return JSON text
   */
  @McpTool(
      name = "list-prometheus-label-values",
      description =
          CONTEXT
              + "List values for one label (GET /api/v1/label/{name}/values). "
              + "Required: name. Optional: start, end, matches (match[]), limit.")
  public String listPrometheusLabelValues(
      String name,
      @Nullable String start,
      @Nullable String end,
      @Nullable List<String> matches,
      @Nullable Integer limit) {
    log.debug(
        "list-prometheus-label-values name={} start={} end={} matches={} limit={}",
        name,
        start,
        end,
        matches,
        limit);
    return prometheusMetricService.listLabelValues(name, start, end, matches, limit);
  }

  /**
   * Exposes {@link PrometheusMetricService#listSeries(List, String, String, Integer)}.
   *
   * @param matches required non-empty list of series selectors (match[])
   * @param start   optional
   * @param end     optional
   * @param limit   optional
   * @return JSON text
   */
  @McpTool(
      name = "list-prometheus-series",
      description =
          CONTEXT
              + "Find series by label matchers (GET /api/v1/series). "
              + "Required: matches (non-empty match[] list). Optional: start, end, limit.")
  public String listPrometheusSeries(
      List<String> matches,
      @Nullable String start,
      @Nullable String end,
      @Nullable Integer limit) {
    log.debug("list-prometheus-series matches={} start={} end={} limit={}", matches, start, end, limit);
    return prometheusMetricService.listSeries(matches, start, end, limit);
  }

  /**
   * Exposes {@link PrometheusMetricService#getTargets(String, String)}.
   *
   * @param scrapePool optional scrape job pool name
   * @param state      optional active | dropped | any
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-targets",
      description =
          CONTEXT
              + "Scrape targets and health (GET /api/v1/targets): lastError, lastScrape, health, URLs. "
              + "Optional: scrapePool, state (active|dropped|any).")
  public String prometheusTargets(
      @Nullable String scrapePool, @Nullable String state) {
    log.debug("prometheus-targets scrapePool={} state={}", scrapePool, state);
    return prometheusMetricService.getTargets(scrapePool, state);
  }

  /**
   * Exposes {@link PrometheusMetricService#getAlerts()}.
   *
   * @return JSON text with active alerts
   */
  @McpTool(
      name = "prometheus-alerts",
      description =
          CONTEXT
              + "Active alerts evaluated in Prometheus (GET /api/v1/alerts): labels, annotations, state.")
  public String prometheusAlerts() {
    log.debug("prometheus-alerts");
    return prometheusMetricService.getAlerts();
  }

  /**
   * Exposes {@link PrometheusMetricService#getRules(String, List, List, List, List, String, Integer, String)}.
   *
   * @param type            optional alert | record
   * @param ruleNames       optional rule_name[]
   * @param ruleGroups      optional rule_group[]
   * @param files           optional file[]
   * @param matches         optional match[]
   * @param excludeAlerts   optional
   * @param groupLimit      optional
   * @param groupNextToken  optional pagination
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-rules",
      description =
          CONTEXT
              + "Alerting and recording rules (GET /api/v1/rules). "
              + "Optional: type (alert|record), ruleNames (rule_name[]), ruleGroups (rule_group[]), "
              + "files (file[]), matches (match[]), excludeAlerts, groupLimit, groupNextToken.")
  public String prometheusRules(
      @Nullable String type,
      @Nullable List<String> ruleNames,
      @Nullable List<String> ruleGroups,
      @Nullable List<String> files,
      @Nullable List<String> matches,
      @Nullable String excludeAlerts,
      @Nullable Integer groupLimit,
      @Nullable String groupNextToken) {
    log.debug("prometheus-rules type={}", type);
    return prometheusMetricService.getRules(
        type,
        ruleNames,
        ruleGroups,
        files,
        matches,
        excludeAlerts,
        groupLimit,
        groupNextToken);
  }

  /**
   * Exposes {@link PrometheusMetricService#getMetadata(Integer, Integer, String)}.
   *
   * @param limit           optional
   * @param limitPerMetric optional limit_per_metric
   * @param metric          optional metric name filter
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-metadata",
      description =
          CONTEXT
              + "Metric metadata HELP/TYPE/unit (GET /api/v1/metadata). "
              + "Optional: limit, limitPerMetric (limit_per_metric), metric.")
  public String prometheusMetadata(
      @Nullable Integer limit,
      @Nullable Integer limitPerMetric,
      @Nullable String metric) {
    log.debug("prometheus-metadata metric={}", metric);
    return prometheusMetricService.getMetadata(limit, limitPerMetric, metric);
  }

  /**
   * Exposes {@link PrometheusMetricService#getScrapePools()}.
   *
   * @return JSON text with scrape pool names
   */
  @McpTool(
      name = "prometheus-scrape-pools",
      description = CONTEXT + "List scrape pool names (GET /api/v1/scrape_pools).")
  public String prometheusScrapePools() {
    log.debug("prometheus-scrape-pools");
    return prometheusMetricService.getScrapePools();
  }

  /**
   * Exposes {@link PrometheusMetricService#getTargetsMetadata(String, String, Integer)}.
   *
   * @param matchTarget optional match_target selector
   * @param metric      optional metric name
   * @param limit       optional
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-targets-metadata",
      description =
          CONTEXT
              + "Per-target metric metadata (GET /api/v1/targets/metadata). "
              + "Optional: matchTarget (match_target), metric, limit.")
  public String prometheusTargetsMetadata(
      @Nullable String matchTarget, @Nullable String metric, @Nullable Integer limit) {
    log.debug("prometheus-targets-metadata matchTarget={} metric={}", matchTarget, metric);
    return prometheusMetricService.getTargetsMetadata(matchTarget, metric, limit);
  }

  /**
   * Exposes {@link PrometheusMetricService#getAlertManagers()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-alertmanagers",
      description =
          CONTEXT
              + "Alertmanager discovery from Prometheus (GET /api/v1/alertmanagers): active and dropped URLs.")
  public String prometheusAlertmanagers() {
    log.debug("prometheus-alertmanagers");
    return prometheusMetricService.getAlertManagers();
  }

  /**
   * Exposes {@link PrometheusMetricService#queryExemplars(String, String, String)}.
   *
   * @param query required PromQL for exemplars
   * @param start optional range start
   * @param end   optional range end
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-query-exemplars",
      description =
          CONTEXT
              + "Query exemplars / trace links (GET /api/v1/query_exemplars). "
              + "Required: query. Optional: start, end (RFC3339 or Unix seconds).")
  public String prometheusQueryExemplars(
      String query, @Nullable String start, @Nullable String end) {
    log.debug("prometheus-query-exemplars query={} start={} end={}", query, start, end);
    return prometheusMetricService.queryExemplars(query, start, end);
  }

  /**
   * Exposes {@link PrometheusMetricService#formatQuery(String)}.
   *
   * @param query PromQL to format
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-format-query",
      description = CONTEXT + "Format a PromQL expression (GET /api/v1/format_query). Required: query.")
  public String prometheusFormatQuery(String query) {
    log.debug("prometheus-format-query");
    return prometheusMetricService.formatQuery(query);
  }

  /**
   * Exposes {@link PrometheusMetricService#parseQuery(String)}.
   *
   * @param query PromQL to parse
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-parse-query",
      description = CONTEXT + "Parse a PromQL expression (GET /api/v1/parse_query). Required: query.")
  public String prometheusParseQuery(String query) {
    log.debug("prometheus-parse-query");
    return prometheusMetricService.parseQuery(query);
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusBuildInfo()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-buildinfo",
      description = CONTEXT + "Prometheus build version and revision (GET /api/v1/status/buildinfo).")
  public String prometheusStatusBuildInfo() {
    log.debug("prometheus-status-buildinfo");
    return prometheusMetricService.getStatusBuildInfo();
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusRuntimeInfo()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-runtimeinfo",
      description =
          CONTEXT
              + "Runtime info: start time, config reload, retention (GET /api/v1/status/runtimeinfo).")
  public String prometheusStatusRuntimeInfo() {
    log.debug("prometheus-status-runtimeinfo");
    return prometheusMetricService.getStatusRuntimeInfo();
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusFlags()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-flags",
      description = CONTEXT + "Effective Prometheus command-line flags (GET /api/v1/status/flags).")
  public String prometheusStatusFlags() {
    log.debug("prometheus-status-flags");
    return prometheusMetricService.getStatusFlags();
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusTsdb(Integer)}.
   *
   * @param limit optional cap per TSDB status category
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-tsdb",
      description =
          CONTEXT
              + "TSDB statistics: head, cardinality (GET /api/v1/status/tsdb). Optional: limit.")
  public String prometheusStatusTsdb(@Nullable Integer limit) {
    log.debug("prometheus-status-tsdb limit={}", limit);
    return prometheusMetricService.getStatusTsdb(limit);
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusTsdbBlocks()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-tsdb-blocks",
      description = CONTEXT + "TSDB on-disk blocks (GET /api/v1/status/tsdb/blocks).")
  public String prometheusStatusTsdbBlocks() {
    log.debug("prometheus-status-tsdb-blocks");
    return prometheusMetricService.getStatusTsdbBlocks();
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusWalReplay()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-status-walreplay",
      description = CONTEXT + "WAL replay status (GET /api/v1/status/walreplay).")
  public String prometheusStatusWalReplay() {
    log.debug("prometheus-status-walreplay");
    return prometheusMetricService.getStatusWalReplay();
  }

  /**
   * Exposes {@link PrometheusMetricService#getStatusConfig()}.
   *
   * @return JSON text (may include sensitive scrape configuration)
   */
  @McpTool(
      name = "prometheus-status-config",
      description =
          CONTEXT
              + "Full running prometheus.yml (GET /api/v1/status/config). "
              + "Sensitive: may expose targets and relabeling; use only when necessary.")
  public String prometheusStatusConfig() {
    log.debug("prometheus-status-config");
    return prometheusMetricService.getStatusConfig();
  }

  /**
   * Exposes {@link PrometheusMetricService#getNotifications()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-notifications",
      description =
          CONTEXT + "Server notifications such as failed reload (GET /api/v1/notifications).")
  public String prometheusNotifications() {
    log.debug("prometheus-notifications");
    return prometheusMetricService.getNotifications();
  }

  /**
   * Exposes {@link PrometheusMetricService#getFeatures()}.
   *
   * @return JSON text
   */
  @McpTool(
      name = "prometheus-features",
      description = CONTEXT + "Enabled Prometheus feature flags (GET /api/v1/features).")
  public String prometheusFeatures() {
    log.debug("prometheus-features");
    return prometheusMetricService.getFeatures();
  }
}
