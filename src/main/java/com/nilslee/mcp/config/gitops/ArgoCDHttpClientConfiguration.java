package com.nilslee.mcp.config.gitops;

import com.nilslee.mcp.service.cluster.ClusterResourceService;
import com.nilslee.mcp.service.gitops.query.ArgoCDQueries;
import com.nilslee.mcp.service.gitops.auth.ArgoCDAuthQueries;
import com.nilslee.mcp.service.gitops.auth.ArgoCdSessionRequest;
import com.nilslee.mcp.service.gitops.auth.ArgoCdUpdatePasswordRequest;
import com.nilslee.mcp.service.gitops.auth.SecurityUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.Map;

/**
 * Wires Argo CD HTTP service clients: {@code argocd-auth} for session and password APIs, {@code argocd-data}
 * for read APIs. On startup, ensures the MCP service account password exists in Kubernetes (see
 * {@link ArgoCDConfigurationProperties#serviceAccountSecretName()}); if missing, rotates it using the initial
 * admin secret then stores the generated password. The data RestClient is given a bearer token from
 * {@link ArgoCDAuthQueries#createSessionToken}.
 */
@Configuration
@EnableConfigurationProperties(ArgoCDConfigurationProperties.class)
@ImportHttpServices(group="argocd-auth", types = ArgoCDAuthQueries.class)
@ImportHttpServices(group="argocd-data", types = ArgoCDQueries.class)
public class ArgoCDHttpClientConfiguration {

  private final ArgoCDConfigurationProperties props;

  private final ArgoCDAuthQueries authQueries;
  private final ClusterResourceService  clusterResourceService;
  private final SecurityUtils securityUtils;

  private String serviceAccountPassword;

  public ArgoCDHttpClientConfiguration(
      ArgoCDConfigurationProperties props,
      ArgoCDAuthQueries authQueries,
      @Lazy ClusterResourceService clusterResourceService,
      SecurityUtils securityUtils) {
    this.props = props;
    this.authQueries = authQueries;
    this.clusterResourceService = clusterResourceService;
    this.securityUtils = securityUtils;
  }

  @PostConstruct
  public void postConstruct() {
    // Update service account password with randomly generated SHA
    serviceAccountPassword = getServiceAccountPassword();
  }

  /**
   * Adds {@code Authorization: Bearer &lt;token&gt;} to every {@code argocd-data} HTTP service call using a
   * session obtained with the resolved service-account password.
   */
  @Bean
  RestClientHttpServiceGroupConfigurer argocdRestClientHttpServiceGroupConfigurer() {
    return groups -> {
      groups
        .filterByName("argocd-data")
        .forEachClient((group, clientBuilder) -> {
          String sessionToken =
              authQueries
                  .createSessionToken(new ArgoCdSessionRequest(props.serviceAccountName(), serviceAccountPassword))
                  .token();
          clientBuilder.defaultHeaders(headers -> headers.setBearerAuth(sessionToken));
        });
    };
  }

  private String getServiceAccountPassword() {
    // Return existing password if secret exists
    String existingPassword = clusterResourceService.getSecretValue(props.namespace(), props.serviceAccountSecretName(), "password");
    if (existingPassword != null) {
      return existingPassword;
    }

    // Generate new password and store as secret
    String password = securityUtils.generatePassword(14);

    String initialAdminPassword =
        clusterResourceService.getSecretValue(props.namespace(), props.initialSecretName(), "password");
    authQueries.updatePassword(
        new ArgoCdUpdatePasswordRequest(props.serviceAccountName(), password, initialAdminPassword));

    Map<String, String> secretMap =  Map.of("password", password);
    clusterResourceService.setSecret(props.namespace(), props.serviceAccountSecretName(), secretMap);

    return password;
  }
}

