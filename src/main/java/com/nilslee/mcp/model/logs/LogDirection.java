package com.nilslee.mcp.model.logs;

/**
 * Typed enum for log direction to prevent prompt-injection via free-text direction strings.
 */
public enum LogDirection {
  FORWARD,
  BACKWARD
}
