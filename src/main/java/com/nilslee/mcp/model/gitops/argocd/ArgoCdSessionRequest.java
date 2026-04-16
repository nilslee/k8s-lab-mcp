package com.nilslee.mcp.model.gitops.argocd;

/**
 * JSON body for Argo CD {@code POST /api/v1/session}.
 */
public record ArgoCdSessionRequest(String username, String password) {}
