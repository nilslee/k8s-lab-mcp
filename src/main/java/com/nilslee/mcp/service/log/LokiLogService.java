package com.nilslee.mcp.service.log;

import tools.jackson.databind.json.JsonMapper;
import com.nilslee.mcp.model.log.LogDirection;
import com.nilslee.mcp.service.log.query.LogQueries;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

/**
 * Loki log discovery and query facade for the k8s-lab stack (Promtail → Loki). All timestamps passed
 * to Loki are Unix epoch <strong>nanoseconds</strong> ({@code long}), matching {@code /loki/api/v1/*}
 * query parameters.
 */
@Service
public class LokiLogService {

  private final LogQueries logQueries;
  private final JsonMapper jsonMapper;

  public LokiLogService(LogQueries logQueries, JsonMapper jsonMapper) {
    this.logQueries = logQueries;
    this.jsonMapper = jsonMapper;
  }

  /**
   * Lists label names present in Loki.
   *
   * @param namespace optional; when set, sent as Loki {@code match={namespace="…"}} to scope discovery
   */
  public String listLokiLabels(
      @Nullable Long startNanosInclusive,
      @Nullable Long endNanosInclusive,
      @Nullable String namespace) {
    try {
      String match = null;
      if (namespace != null && !namespace.isBlank()) {
        match = namespaceMatchSelector(namespace);
      }
      return logQueries.labels(startNanosInclusive, endNanosInclusive, match);
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  public String listLokiLabelValues(
      String labelName,
      @Nullable Long startNanosInclusive,
      @Nullable Long endNanosInclusive,
      @Nullable String namespace) {
    try {
      String match = null;
      if (namespace != null && !namespace.isBlank()) {
        match = namespaceMatchSelector(namespace);
      }
      return logQueries.labelValues(labelName, startNanosInclusive, endNanosInclusive, match);
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  public String listLokiSeries(
      String streamSelector,
      long startNanosInclusive,
      long endNanosInclusive,
      @Nullable List<String> additionalStreamSelectors) {
    try {
      List<String> matches = new ArrayList<>();
      matches.add(streamSelector);
      if (additionalStreamSelectors != null) {
        for (String extra : additionalStreamSelectors) {
          if (extra != null && !extra.isBlank()) {
            matches.add(extra);
          }
        }
      }
      return logQueries.series(matches, startNanosInclusive, endNanosInclusive);
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  public String queryLogs(
      String query,
      long startNanosInclusive,
      long endNanosInclusive,
      @Nullable Integer limit,
      @Nullable LogDirection direction) {
    try {
      String dir = direction != null ? direction.name().toLowerCase() : LogDirection.BACKWARD.name().toLowerCase();
      return logQueries.queryRange(query, startNanosInclusive, endNanosInclusive, limit, dir);
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  public String tailLogs(
      String query,
      long timeNanos,
      @Nullable Integer limit,
      @Nullable LogDirection direction,
      @Nullable Integer delayForSeconds) {
    try {
      String dir = direction != null ? direction.name().toLowerCase() : LogDirection.BACKWARD.name().toLowerCase();
      return logQueries.query(query, timeNanos, limit, dir, delayForSeconds);
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  private static String namespaceMatchSelector(String namespace) {
    String escaped = namespace.replace("\\", "\\\\").replace("\"", "\\\"");
    return "{namespace=\"" + escaped + "\"}";
  }
}
