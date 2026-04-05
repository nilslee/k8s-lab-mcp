package com.nilslee.mcp.config.response;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Applies {@link McpToolJsonTextSafeguard} to every {@link McpTool} method that returns a
 * {@link String}, so JSON survives buggy MCP clients without per-tool code.
 */
@Aspect
@Component
@ConditionalOnProperty(
    prefix = "mcp.tool-response",
    name = "serialize-top-level-data-json-array",
    havingValue = "true",
    matchIfMissing = true)
public class McpToolResponseSafeguardAspect {

  private final JsonMapper jsonMapper;

  public McpToolResponseSafeguardAspect(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Around("@annotation(org.springframework.ai.mcp.annotation.McpTool)")
  public Object aroundMcpTool(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    if (result instanceof String s) {
      return McpToolJsonTextSafeguard.apply(s, jsonMapper);
    }
    return result;
  }
}
