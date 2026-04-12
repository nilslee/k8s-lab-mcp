package com.nilslee.mcp.model.gitops;

/**
 * JSON response from Argo CD {@code POST /api/v1/session}.
 */
public record ArgoCdSessionResponse(String token) {}
