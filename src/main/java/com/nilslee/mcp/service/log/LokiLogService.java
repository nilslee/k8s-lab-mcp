package com.nilslee.mcp.service.log;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.nilslee.mcp.model.log.LogDirection;
import com.nilslee.mcp.service.log.query.LogQueries;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
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
      String raw =
          utf8(
              logQueries.labels(
                  optionalNanosOrNull(startNanosInclusive),
                  optionalNanosOrNull(endNanosInclusive),
                  match));
      return renameTopLevelDataStringArray(raw, "labelNames");
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
      String raw =
          utf8(
              logQueries.labelValues(
                  labelName,
                  optionalNanosOrNull(startNanosInclusive),
                  optionalNanosOrNull(endNanosInclusive),
                  match));
      return renameTopLevelDataStringArray(raw, "values");
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
      return utf8(logQueries.series(matches, startNanosInclusive, endNanosInclusive));
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
      return utf8(logQueries.queryRange(query, startNanosInclusive, endNanosInclusive, limit, dir));
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
      return utf8(logQueries.query(query, timeNanos, limit, dir, delayForSeconds));
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

  private static String utf8(byte[] body) {
    if (body == null || body.length == 0) {
      return "";
    }
    return new String(body, StandardCharsets.UTF_8);
  }

  /**
   * Some MCP clients mishandle tool text that looks like JSON with a top-level {@code data} array
   * (they keep {@code status} but drop the array). Loki’s label APIs use that shape. We expose the same
   * strings under a neutral key so the payload survives; native Loki uses {@code data}.
   */
  private String renameTopLevelDataStringArray(String raw, String newKey) {
    try {
      JsonNode root = jsonMapper.readTree(raw);
      if (!(root instanceof ObjectNode object)) {
        return raw;
      }
      JsonNode data = object.get("data");
      if (data == null || !data.isArray()) {
        return raw;
      }
      object.set(newKey, data);
      object.remove("data");
      return jsonMapper.writeValueAsString(object);
    } catch (Exception e) {
      return raw;
    }
  }
}
