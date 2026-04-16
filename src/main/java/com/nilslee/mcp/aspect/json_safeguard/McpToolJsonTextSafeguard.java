package com.nilslee.mcp.aspect.json_safeguard;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Rewrites JSON tool payloads that match a shape some MCP clients mishandle: a top-level {@code data}
 * property whose value is a JSON array. Those clients often keep other fields but drop the array.
 * Encoding the array as a JSON string under {@code data} avoids that while keeping the key.
 */
public final class McpToolJsonTextSafeguard {

  private McpToolJsonTextSafeguard() {}

  /**
   * If {@code text} is a JSON object with {@code data} as an array, replaces {@code data} with the
   * array’s JSON text; otherwise returns {@code text} unchanged (including parse failures).
   */
  public static String apply(String text, JsonMapper jsonMapper) {
    if (text == null || text.isBlank()) {
      return text;
    }
    try {
      JsonNode root = jsonMapper.readTree(text);
      if (!(root instanceof ObjectNode object)) {
        return text;
      }
      JsonNode data = object.get("data");
      if (data == null || !data.isArray()) {
        return text;
      }
      object.put("data", jsonMapper.writeValueAsString(data));
      return jsonMapper.writeValueAsString(object);
    } catch (Exception e) {
      return text;
    }
  }
}
