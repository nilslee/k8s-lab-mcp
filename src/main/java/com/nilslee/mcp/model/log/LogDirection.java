package com.nilslee.mcp.model.log;

/**
 * Typed enum for log direction to prevent prompt-injection via free-text direction strings.
 */
public enum LogDirection {
  FORWARD,
  BACKWARD
}
