package com.nilslee.mcp.service.logs.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * Declarative Loki read API ({@code /loki/api/v1/*}). Base URL and auth are configured on the
 * backing {@link org.springframework.web.client.RestClient} bean.
 *
 * <p>Responses use {@code byte[]} so the HTTP client reads the raw body. Declaring {@link String}
 * with {@code Content-Type: application/json} can route through Jackson and drop or reshape fields
 * (for example Loki’s {@code data} array on label endpoints).
 */
@HttpExchange
public interface LokiLogQueries {

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
}
