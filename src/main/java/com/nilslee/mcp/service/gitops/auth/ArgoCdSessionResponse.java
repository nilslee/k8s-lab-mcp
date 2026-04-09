package com.nilslee.mcp.service.gitops.auth;

/**
 * JSON response from Argo CD {@code POST /api/v1/session}.
 */
public record ArgoCdSessionResponse(String token) {}
