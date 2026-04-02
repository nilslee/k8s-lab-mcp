package com.nilslee.mcp.cluster;

/**
 * Typed enum for pod phase filtering to prevent prompt-injection via free-text status strings.
 */
public enum PodPhaseFilter {
    ALL,
    RUNNING,
    PENDING,
    FAILED,
    SUCCEEDED,
    UNKNOWN
}
