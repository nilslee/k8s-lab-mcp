package com.nilslee.mcp.exception;

/**
 * Thrown when a model-supplied input fails validation before any Kubernetes API call is made.
 */
public class InvalidClusterInputException extends RuntimeException {

    public InvalidClusterInputException(String message) {
        super(message);
    }
}
