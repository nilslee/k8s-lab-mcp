package com.nilslee.mcp.service.logs;

import tools.jackson.databind.json.JsonMapper;
import com.nilslee.mcp.model.logs.LogDirection;
import com.nilslee.mcp.service.logs.query.LogQueries;
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

  /** Lookback window for {@link #tailLogs}: Loki log queries require {@code query_range}, not instant query. */
  private static final long TAIL_LOOKBACK_NANOS = 3600L * 1_000_000_000L;

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
      return logQueries.labels(
          optionalNanosOrNull(startNanosInclusive),
          optionalNanosOrNull(endNanosInclusive),
          match);
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
      return logQueries.labelValues(
          labelName,
          optionalNanosOrNull(startNanosInclusive),
          optionalNanosOrNull(endNanosInclusive),
          match);
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

  /**
   * Latest log lines near an instant using {@code query_range} (backward). Loki rejects log selectors on
   * instant {@code /query}; this uses a fixed lookback ending at {@code asOfEndNanos} (minus optional ingest
   * lag).
   */
  public String tailLogs(
      String query,
      long asOfEndNanos,
      @Nullable Integer limit,
      @Nullable Integer delayForSeconds) {
    try {
      long delayNanos =
          (delayForSeconds != null && delayForSeconds > 0)
              ? delayForSeconds * 1_000_000_000L
              : 0L;
      long endNanos = asOfEndNanos - delayNanos;
      if (endNanos < 0) {
        endNanos = 0;
      }
      long startNanos = endNanos - TAIL_LOOKBACK_NANOS;
      if (startNanos < 0) {
        startNanos = 0;
      }
      if (startNanos >= endNanos) {
        if (endNanos == 0) {
          endNanos = 1;
          startNanos = 0;
        } else {
          startNanos = endNanos - 1;
        }
      }
      return logQueries.queryRange(
          query, startNanos, endNanos, limit, LogDirection.BACKWARD.name().toLowerCase());
    } catch (RestClientException ex) {
      return LokiErrorResponses.fromException(ex, jsonMapper);
    }
  }

  private static String namespaceMatchSelector(String namespace) {
    String escaped = namespace.replace("\\", "\\\\").replace("\"", "\\\"");
    return "{namespace=\"" + escaped + "\"}";
  }

  /**
   * Loki’s optional {@code start}/{@code end} on label APIs default to a recent window when omitted.
   * Sending {@code 0} (common JSON “unset” from clients) becomes {@code start=0&end=0}, i.e. a
   * degenerate range at the epoch with no streams; some Loki builds then return {@code success}
   * with no {@code data} field. Non-positive values are therefore treated as unspecified.
   */
  private static @Nullable Long optionalNanosOrNull(@Nullable Long nanos) {
    if (nanos == null || nanos <= 0) {
      return null;
    }
    return nanos;
  }
}
