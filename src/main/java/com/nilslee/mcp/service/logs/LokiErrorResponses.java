package com.nilslee.mcp.service.logs;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

/**
 * Maps Loki / proxy HTTP failures to concise MCP-facing messages.
 */
public final class LokiErrorResponses {

  private LokiErrorResponses() {}

  public static String fromException(RestClientException ex, JsonMapper jsonMapper) {
    if (ex instanceof RestClientResponseException responseEx) {
      return fromResponse(responseEx, jsonMapper);
    }
    return "Loki request failed: " + ex.getMessage();
  }

  public static String fromResponse(RestClientResponseException ex, JsonMapper jsonMapper) {
    int status = ex.getStatusCode().value();
    String body = ex.getResponseBodyAsString(StandardCharsets.UTF_8);
    if (status == 401 || status == 403) {
      return "Loki/Grafana returned HTTP "
          + status
          + " (check mcp.monitoring.loki username/password and datasource access). Body: "
          + truncate(body);
    }
    String lokiMsg = parseLokiError(body, jsonMapper);
    if (lokiMsg != null) {
      return "Loki error (HTTP " + status + "): " + lokiMsg;
    }
    return "Loki request failed (HTTP " + status + "): " + truncate(body);
  }

  private static @Nullable String parseLokiError(String body, JsonMapper mapper) {
    if (body == null || body.isBlank()) {
      return null;
    }
    try {
      JsonNode root = mapper.readTree(body);
      if (root.has("message") && root.get("message").isTextual()) {
        return root.get("message").asText();
      }
      if ("error".equalsIgnoreCase(text(root, "status"))) {
        String err = text(root, "error");
        if (err != null) {
          return err;
        }
      }
      if (root.has("error") && root.get("error").isTextual()) {
        return root.get("error").asText();
      }
    } catch (Exception ignored) {
      // fall through
    }
    return null;
  }

  private static @Nullable String text(JsonNode node, String field) {
    if (node.has(field) && node.get(field).isTextual()) {
      return node.get(field).asText();
    }
    return null;
  }

  private static String truncate(String s) {
    if (s.length() <= 2000) {
      return s;
    }
    return s.substring(0, 2000) + "…";
  }
}
