package com.nilslee.mcp.service.gitops.auth;

/**
 * JSON body for Argo CD {@code PUT /api/v1/account/password}.
 */
public record ArgoCdUpdatePasswordRequest(String name, String newPassword, String currentPassword) {}
