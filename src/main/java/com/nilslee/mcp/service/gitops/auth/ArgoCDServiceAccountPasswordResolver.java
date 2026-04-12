package com.nilslee.mcp.service.gitops.auth;

import com.nilslee.mcp.config.gitops.ArgoCDConfigurationProperties;
import com.nilslee.mcp.model.gitops.ArgoCdSessionRequest;
import com.nilslee.mcp.model.gitops.ArgoCdSessionResponse;
import com.nilslee.mcp.model.gitops.ArgoCdUpdatePasswordRequest;
import com.nilslee.mcp.service.cluster.ClusterResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Ensures the MCP Argo CD local-user password exists in Kubernetes: reads the configured secret, or generates a
 * password, calls Argo CD to set it (using an authenticated admin session), then stores it in the cluster.
 *
 * <p>Argo CD's {@code UpdatePassword} RPC requires a logged-in caller; it checks {@code currentPassword} against the
 * <em>authenticated</em> user (admin), not the target account. Unauthenticated {@code PUT /account/password} calls
 * never set the automation user's password.
 */
@Component
public class ArgoCDServiceAccountPasswordResolver {

  private static final Logger log = LoggerFactory.getLogger(ArgoCDServiceAccountPasswordResolver.class);

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
    if (existingPassword != null && !existingPassword.isBlank()) {
      return existingPassword;
    }

    String initialAdminPassword =
        clusterResourceService.getSecretValue(props.namespace(), props.initialSecretName(), "password");
    if (initialAdminPassword == null || initialAdminPassword.isBlank()) {
      throw new IllegalStateException(
          "Cannot bootstrap Argo CD MCP user: missing key password in Secret "
              + props.initialSecretName()
              + " in namespace "
              + props.namespace());
    }

    String password = securityUtils.generatePassword(14);

    ArgoCdSessionResponse adminSession =
        authQueries.createSessionToken(
            new ArgoCdSessionRequest(props.bootstrapAdminUsername(), initialAdminPassword));
    String adminToken = adminSession.token();
    if (adminToken == null || adminToken.isBlank()) {
      throw new IllegalStateException(
          "Cannot bootstrap Argo CD MCP user: admin POST /api/v1/session returned no token (check "
              + props.bootstrapAdminUsername()
              + " password in Secret "
              + props.initialSecretName()
              + ").");
    }

    authQueries.updatePassword(
        "Bearer " + adminToken,
        new ArgoCdUpdatePasswordRequest(
            props.serviceAccountName(), password, initialAdminPassword));

    Map<String, String> secretMap = Map.of("password", password);
    clusterResourceService.setSecret(props.namespace(), props.serviceAccountSecretName(), secretMap);
    log.info(
        "Bootstrapped Argo CD local user '{}' and stored password in Secret {}/{}",
        props.serviceAccountName(),
        props.namespace(),
        props.serviceAccountSecretName());

    return password;
  }
}
