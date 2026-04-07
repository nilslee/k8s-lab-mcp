package com.nilslee.mcp.service.metrics;

import com.nilslee.mcp.service.metrics.query.PrometheusMetricQueries;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Thin façade over the Prometheus HTTP API v1 ({@code /api/v1/*}) for read-only diagnostics.
 * Delegates to {@link PrometheusMetricQueries}; responses are JSON text as returned by Prometheus.
 *
 * <p>Timestamps follow Prometheus: RFC3339 strings or Unix seconds, per endpoint (see each method).
 */
@Service
public class PrometheusMetricService {

  /** Low-level HTTP client interface; exposed for advanced callers or tests. */
  public final PrometheusMetricQueries prometheusMetricQueries;

  public PrometheusMetricService(PrometheusMetricQueries prometheusMetricQueries) {
    this.prometheusMetricQueries = prometheusMetricQueries;
  }

  /**
   * Instant PromQL evaluation ({@code GET /api/v1/query}).
   *
   * @param query         required PromQL expression
   * @param limit         optional max metrics to return
   * @param time          optional evaluation instant (RFC3339 or Unix seconds); default is “now”
   * @param timeout       optional evaluation timeout (e.g. {@code 30s})
   * @param lookbackDelta optional override of lookback for range selectors inside the query
   * @param stats         optional; use {@code all} for query statistics in the response
   * @return Prometheus JSON body ({@code status}, {@code data}, etc.)
   */
  public String query(
      String query,
      @Nullable Integer limit,
      @Nullable String time,
      @Nullable String timeout,
      @Nullable String lookbackDelta,
      @Nullable String stats) {
    return prometheusMetricQueries.query(query, limit, time, timeout, lookbackDelta, stats);
  }

  /**
   * Range PromQL evaluation ({@code GET /api/v1/query_range}).
   *
   * @param query         required PromQL expression (typically a range or aggregation over a range)
   * @param start         range start (RFC3339 or Unix seconds)
   * @param end           range end (RFC3339 or Unix seconds)
   * @param step            resolution step width (duration string or seconds)
   * @param limit         optional max series
   * @param timeout       optional evaluation timeout
   * @param lookbackDelta optional lookback override
   * @param stats         optional statistics ({@code all})
   * @return Prometheus JSON body with matrix result type when applicable
   */
  public String queryRange(
      String query,
      String start,
      String end,
      String step,
      @Nullable Integer limit,
      @Nullable String timeout,
      @Nullable String lookbackDelta,
      @Nullable String stats) {
    return prometheusMetricQueries.queryRange(
        query, start, end, step, limit, timeout, lookbackDelta, stats);
  }

  /**
   * Label names present for the optional time range and series matchers ({@code GET /api/v1/labels}).
   *
   * @param matches optional {@code match[]} selectors (e.g. {@code {job="prometheus"}})
   * @param start   optional range start
   * @param end     optional range end
   * @param limit   optional max label names
   * @return JSON with {@code data} as list of label names
   */
  public String listLabels(
      @Nullable List<String> matches,
      @Nullable String start,
      @Nullable String end,
      @Nullable Integer limit) {
    return prometheusMetricQueries.labels(matches, start, end, limit);
  }

  /**
   * Values for one label name ({@code GET /api/v1/label/{name}/values}).
   *
   * @param name    label name (path segment)
   * @param start   optional range start
   * @param end     optional range end
   * @param matches optional {@code match[]} selectors
   * @param limit   optional max values
   * @return JSON with {@code data} as list of string values
   */
  public String listLabelValues(
      String name,
      @Nullable String start,
      @Nullable String end,
      @Nullable List<String> matches,
      @Nullable Integer limit) {
    return prometheusMetricQueries.labelValues(name, start, end, matches, limit);
  }

  /**
   * Series matching label matchers ({@code GET /api/v1/series}).
   *
   * @param matches required non-empty {@code match[]} list (selectors)
   * @param start   optional start time
   * @param end     optional end time
   * @param limit   optional max series
   * @return JSON with {@code data} as list of label sets
   */
  public String listSeries(
      List<String> matches,
      @Nullable String start,
      @Nullable String end,
      @Nullable Integer limit) {
    return prometheusMetricQueries.series(matches, start, end, limit);
  }

  /**
   * Scrape targets and health ({@code GET /api/v1/targets}).
   *
   * @param scrapePool optional filter by scrape job pool name
   * @param state      optional {@code active}, {@code dropped}, or {@code any}
   * @return JSON with active/dropped targets and metadata
   */
  public String getTargets(@Nullable String scrapePool, @Nullable String state) {
    return prometheusMetricQueries.targets(scrapePool, state);
  }

  /**
   * Currently active alerts evaluated in Prometheus ({@code GET /api/v1/alerts}).
   *
   * @return JSON with {@code data.alerts} and rule state
   */
  public String getAlerts() {
    return prometheusMetricQueries.alerts();
  }

  /**
   * Alerting and recording rules ({@code GET /api/v1/rules}).
   *
   * @param type            optional {@code alert} or {@code record}
   * @param ruleNames       optional {@code rule_name[]} filters
   * @param ruleGroups      optional {@code rule_group[]} filters
   * @param files           optional {@code file[]} (rule file paths)
   * @param matches         optional {@code match[]} label matchers on rules
   * @param excludeAlerts   optional; whether to strip active alert instances from the payload
   * @param groupLimit      optional max rule groups
   * @param groupNextToken  optional pagination token
   * @return JSON with rule groups and evaluation metadata
   */
  public String getRules(
      @Nullable String type,
      @Nullable List<String> ruleNames,
      @Nullable List<String> ruleGroups,
      @Nullable List<String> files,
      @Nullable List<String> matches,
      @Nullable String excludeAlerts,
      @Nullable Integer groupLimit,
      @Nullable String groupNextToken) {
    return prometheusMetricQueries.rules(
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
   * Metric metadata (HELP/TYPE/unit) ({@code GET /api/v1/metadata}).
   *
   * @param limit           optional max metrics
   * @param limitPerMetric optional max metadata entries per metric name
   * @param metric          optional filter by metric name
   * @return JSON map of metric name to metadata entries
   */
  public String getMetadata(
      @Nullable Integer limit,
      @Nullable Integer limitPerMetric,
      @Nullable String metric) {
    return prometheusMetricQueries.metadata(limit, limitPerMetric, metric);
  }

  /**
   * List of scrape pool names ({@code GET /api/v1/scrape_pools}).
   *
   * @return JSON with {@code scrapePools} list
   */
  public String getScrapePools() {
    return prometheusMetricQueries.scrapePools();
  }

  /**
   * Per-target metric metadata ({@code GET /api/v1/targets/metadata}).
   *
   * @param matchTarget optional label selector for targets
   * @param metric      optional metric name filter
   * @param limit       optional max targets
   * @return JSON list of target/metric metadata rows
   */
  public String getTargetsMetadata(
      @Nullable String matchTarget, @Nullable String metric, @Nullable Integer limit) {
    return prometheusMetricQueries.targetsMetadata(matchTarget, metric, limit);
  }

  /**
   * Discovered Alertmanager endpoints ({@code GET /api/v1/alertmanagers}).
   *
   * @return JSON with active and dropped Alertmanager URLs
   */
  public String getAlertManagers() {
    return prometheusMetricQueries.alertmanagers();
  }

  /**
   * Exemplars for a PromQL query over an optional time range ({@code GET /api/v1/query_exemplars}).
   *
   * @param query required; metric selector or expression supporting exemplars
   * @param start optional range start
   * @param end   optional range end
   * @return JSON with exemplar series and trace IDs when present
   */
  public String queryExemplars(String query, @Nullable String start, @Nullable String end) {
    return prometheusMetricQueries.queryExemplars(query, start, end);
  }

  /**
   * Pretty-print / normalize a PromQL expression ({@code GET /api/v1/format_query}).
   *
   * @param query raw PromQL
   * @return JSON with formatted query string
   */
  public String formatQuery(String query) {
    return prometheusMetricQueries.formatQuery(query);
  }

  /**
   * Parse a PromQL expression into an AST representation ({@code GET /api/v1/parse_query}).
   *
   * @param query PromQL to parse
   * @return JSON parse result
   */
  public String parseQuery(String query) {
    return prometheusMetricQueries.parseQuery(query);
  }

  /**
   * Prometheus binary version and build metadata ({@code GET /api/v1/status/buildinfo}).
   *
   * @return JSON with version, revision, goVersion, etc.
   */
  public String getStatusBuildInfo() {
    return prometheusMetricQueries.statusBuildInfo();
  }

  /**
   * Process runtime information ({@code GET /api/v1/status/runtimeinfo}).
   *
   * @return JSON with start time, config reload status, retention, etc.
   */
  public String getStatusRuntimeInfo() {
    return prometheusMetricQueries.statusRuntimeInfo();
  }

  /**
   * Effective command-line flags ({@code GET /api/v1/status/flags}).
   *
   * @return JSON map of flag names to values
   */
  public String getStatusFlags() {
    return prometheusMetricQueries.statusFlags();
  }

  /**
   * TSDB statistics ({@code GET /api/v1/status/tsdb}).
   *
   * @param limit optional cap on items per category in the response
   * @return JSON with head stats, label cardinality, series counts, etc.
   */
  public String getStatusTsdb(@Nullable Integer limit) {
    return prometheusMetricQueries.statusTsdb(limit);
  }

  /**
   * TSDB on-disk block information ({@code GET /api/v1/status/tsdb/blocks}).
   *
   * @return JSON block list and stats
   */
  public String getStatusTsdbBlocks() {
    return prometheusMetricQueries.statusTsdbBlocks();
  }

  /**
   * WAL replay progress ({@code GET /api/v1/status/walreplay}).
   *
   * @return JSON with min/current/max WAL segment replay positions
   */
  public String getStatusWalReplay() {
    return prometheusMetricQueries.statusWalReplay();
  }

  /**
   * Full running Prometheus configuration YAML ({@code GET /api/v1/status/config}).
   *
   * <p><strong>Sensitive:</strong> may expose scrape targets and relabel rules; use with care.
   *
   * @return JSON with {@code yaml} configuration text
   */
  public String getStatusConfig() {
    return prometheusMetricQueries.statusConfig();
  }

  /**
   * Server notifications (e.g. failed reload) ({@code GET /api/v1/notifications}).
   *
   * @return JSON list of notification objects
   */
  public String getNotifications() {
    return prometheusMetricQueries.notifications();
  }

  /**
   * Enabled feature flags ({@code GET /api/v1/features}).
   *
   * @return JSON list of feature name strings
   */
  public String getFeatures() {
    return prometheusMetricQueries.features();
  }
}
