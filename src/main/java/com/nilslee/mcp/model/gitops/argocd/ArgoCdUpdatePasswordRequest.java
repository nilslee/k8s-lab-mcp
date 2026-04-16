package com.nilslee.mcp.model.gitops.argocd;

/**
 * JSON body for Argo CD {@code PUT /api/v1/account/password}.
 */
public record ArgoCdUpdatePasswordRequest(String name, String newPassword, String currentPassword) {}
