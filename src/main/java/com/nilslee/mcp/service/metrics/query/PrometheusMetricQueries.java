package com.nilslee.mcp.service.metrics.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface PrometheusMetricQueries {

  @GetExchange("/query")
  String query(
      @RequestParam String query,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String time,
      @RequestParam(required = false) String timeout,
      @RequestParam(name = "lookback_delta", required = false) String lookbackDelta,
      @RequestParam(required = false) String stats);

  @GetExchange("/query_range")
  String queryRange(
      @RequestParam String query,
      @RequestParam String start,
      @RequestParam String end,
      @RequestParam String step,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String timeout,
      @RequestParam(name = "lookback_delta", required = false) String lookbackDelta,
      @RequestParam(required = false) String stats);

  @GetExchange("/labels")
  String labels(
      @RequestParam(name = "match[]", required = false) List<String> matches,
      @RequestParam(required = false) String start,
      @RequestParam(required = false) String end,
      @RequestParam(required = false) Integer limit);

  @GetExchange("/label/{name}/values")
  String labelValues(
      @PathVariable("name") String name,
      @RequestParam(required = false) String start,
      @RequestParam(required = false) String end,
      @RequestParam(name = "match[]", required = false) List<String> matches,
      @RequestParam(required = false) Integer limit);

  @GetExchange("/series")
  String series(
      @RequestParam(name = "match[]") List<String> matches,
      @RequestParam(required = false) String start,
      @RequestParam(required = false) String end,
      @RequestParam(required = false) Integer limit);

  @GetExchange("/targets")
  String targets(
      @RequestParam(required = false) String scrapePool,
      @RequestParam(required = false) String state);

  @GetExchange("/alerts")
  String alerts();

  @GetExchange("/rules")
  String rules(
      @RequestParam(required = false) String type,
      @RequestParam(name = "rule_name[]", required = false) List<String> ruleNames,
      @RequestParam(name = "rule_group[]", required = false) List<String> ruleGroups,
      @RequestParam(name = "file[]", required = false) List<String> files,
      @RequestParam(name = "match[]", required = false) List<String> matches,
      @RequestParam(name = "exclude_alerts", required = false) String excludeAlerts,
      @RequestParam(name = "group_limit", required = false) Integer groupLimit,
      @RequestParam(name = "group_next_token", required = false) String groupNextToken);

  @GetExchange("/metadata")
  String metadata(
      @RequestParam(required = false) Integer limit,
      @RequestParam(name = "limit_per_metric", required = false) Integer limitPerMetric,
      @RequestParam(required = false) String metric);

  @GetExchange("/scrape_pools")
  String scrapePools();

  @GetExchange("/targets/metadata")
  String targetsMetadata(
      @RequestParam(name = "match_target", required = false) String matchTarget,
      @RequestParam(required = false) String metric,
      @RequestParam(required = false) Integer limit);

  @GetExchange("/alertmanagers")
  String alertmanagers();

  @GetExchange("/query_exemplars")
  String queryExemplars(
      @RequestParam String query,
      @RequestParam(required = false) String start,
      @RequestParam(required = false) String end);

  @GetExchange("/format_query")
  String formatQuery(@RequestParam String query);

  @GetExchange("/parse_query")
  String parseQuery(@RequestParam String query);

  @GetExchange("/status/buildinfo")
  String statusBuildInfo();

  @GetExchange("/status/runtimeinfo")
  String statusRuntimeInfo();

  @GetExchange("/status/flags")
  String statusFlags();

  @GetExchange("/status/tsdb")
  String statusTsdb(@RequestParam(required = false) Integer limit);

  @GetExchange("/status/tsdb/blocks")
  String statusTsdbBlocks();

  @GetExchange("/status/walreplay")
  String statusWalReplay();

  @GetExchange("/status/config")
  String statusConfig();

  @GetExchange("/notifications")
  String notifications();

  @GetExchange("/features")
  String features();

}
