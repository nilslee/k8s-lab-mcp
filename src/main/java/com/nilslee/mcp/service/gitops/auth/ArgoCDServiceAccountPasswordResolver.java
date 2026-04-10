package com.nilslee.mcp.service.gitops.auth;

import com.nilslee.mcp.config.gitops.ArgoCDConfigurationProperties;
import com.nilslee.mcp.service.cluster.ClusterResourceService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ensures the MCP Argo CD local-user password exists in Kubernetes: reads the configured secret, or generates a
 * password, calls Argo CD to set it (using the initial admin secret), then stores it in the cluster.
 */
@Component
public class ArgoCDServiceAccountPasswordResolver {

  private final ArgoCDConfigurationProperties props;
  private final ClusterResourceService clusterResourceService;
  private final SecurityUtils securityUtils;

  public ArgoCDServiceAccountPasswordResolver(
      ArgoCDConfigurationProperties props,
      ClusterResourceService clusterResourceService,
      SecurityUtils securityUtils) {
    this.props = props;
    this.clusterResourceService = clusterResourceService;
    this.securityUtils = securityUtils;
  }

  /**
   * @param authQueries resolved only after {@code httpServiceProxyRegistry} has created the auth client
   */
  public String resolve(ArgoCDAuthQueries authQueries) {
    String existingPassword =
        clusterResourceService.getSecretValue(
            props.namespace(), props.serviceAccountSecretName(), "password");
    if (existingPassword != null) {
      return existingPassword;
    }

    String password = securityUtils.generatePassword(14);

    String initialAdminPassword =
        clusterResourceService.getSecretValue(props.namespace(), props.initialSecretName(), "password");
    authQueries.updatePassword(
        new ArgoCdUpdatePasswordRequest(props.serviceAccountName(), password, initialAdminPassword));

    Map<String, String> secretMap = Map.of("password", password);
    clusterResourceService.setSecret(props.namespace(), props.serviceAccountSecretName(), secretMap);

    return password;
  }
}
