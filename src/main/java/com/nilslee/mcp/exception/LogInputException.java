package com.nilslee.mcp.exception;

/**
 * Thrown when a model-supplied input fails validation before any Log API call is made.
 */
public class LogInputException extends RuntimeException {

  public LogInputException(String message) {
    super(message);
  }
}
