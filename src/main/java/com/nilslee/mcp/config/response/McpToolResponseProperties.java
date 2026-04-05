package com.nilslee.mcp.config.response;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional tuning for how string tool results are post-processed before MCP clients see them.
 * 
 * @param serializeTopLevelDataJsonArray When true, JSON text results with a top-level {@code data} array are rewritten so
 * {@code data} holds the array’s JSON as a string (some MCP UIs drop array-valued {@code data}).
 *
 *  <p>Defaults to true.</p>
 */
@ConfigurationProperties(prefix = "mcp.tool-response")
public record McpToolResponseProperties(
    boolean serializeTopLevelDataJsonArray
) {
  public McpToolResponseProperties {
    serializeTopLevelDataJsonArray = true;
  }
}
