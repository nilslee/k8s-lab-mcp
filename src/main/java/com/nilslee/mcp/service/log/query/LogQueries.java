package com.nilslee.mcp.service.log.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * Declarative Loki read API ({@code /loki/api/v1/*}). Base URL and auth are configured on the
 * backing {@link org.springframework.web.client.RestClient} bean.
 */
@HttpExchange
public interface LogQueries {

  @GetExchange("/loki/api/v1/labels")
  String labels(
      @RequestParam(required = false) Long start,
      @RequestParam(required = false) Long end,
      @RequestParam(required = false) String match);

  @GetExchange("/loki/api/v1/label/{label}/values")
  String labelValues(
      @PathVariable("label") String label,
      @RequestParam(required = false) Long start,
      @RequestParam(required = false) Long end,
      @RequestParam(required = false) String match);

  @GetExchange("/loki/api/v1/series")
  String series(
      @RequestParam("match") List<String> matches,
      @RequestParam long start,
      @RequestParam long end);

  @GetExchange("/loki/api/v1/query_range")
  String queryRange(
      @RequestParam String query,
      @RequestParam long start,
      @RequestParam long end,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String direction);

  @GetExchange("/loki/api/v1/query")
  String query(
      @RequestParam String query,
      @RequestParam("time") long timeNanos,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String direction,
      @RequestParam(value = "delay_for", required = false) Integer delayForSeconds);
}
