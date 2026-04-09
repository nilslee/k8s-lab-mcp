package com.nilslee.mcp.config.gitops;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Argo CD integration settings under {@code mcp.gitops.argocd}.
 *
 * @param serviceAccountName       Argo CD local user the MCP server authenticates as
 * @param initialSecretName        Kubernetes secret holding the bootstrap admin password (key {@code password})
 * @param serviceAccountSecretName Kubernetes secret where MCP stores the generated service-account password
 * @param namespace                Namespace where Argo CD (and these secrets) live
 */
@ConfigurationProperties("mcp.gitops.argocd")
public record ArgoCDConfigurationProperties(
    String serviceAccountName,
    String initialSecretName,
    String serviceAccountSecretName,
    String namespace
) {
}
