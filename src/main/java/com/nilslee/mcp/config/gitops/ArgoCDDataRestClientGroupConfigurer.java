package com.nilslee.mcp.config.gitops;

import com.nilslee.mcp.service.gitops.auth.ArgoCDAuthQueries;
import com.nilslee.mcp.service.gitops.auth.ArgoCDServiceAccountPasswordResolver;
import com.nilslee.mcp.service.gitops.auth.ArgoCdSessionRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroupConfigurer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adds {@code Authorization: Bearer} to {@code argocd-data} HTTP service calls.
 *
 * <p>{@link ArgoCDAuthQueries} must not be resolved during {@link #configureGroups} — that still runs while
 * {@code httpServiceProxyRegistry} is initializing. A {@link ClientHttpRequestInterceptor} defers
 * {@link ObjectProvider#getObject()} to the first outbound request, after the context is ready.
 */
@Component
public class ArgoCDDataRestClientGroupConfigurer implements RestClientHttpServiceGroupConfigurer {

  private final ArgoCDConfigurationProperties props;
  private final ArgoCDServiceAccountPasswordResolver passwordResolver;
  private final ObjectProvider<ArgoCDAuthQueries> authQueries;
  private final AtomicReference<String> serviceAccountPassword = new AtomicReference<>();

  public ArgoCDDataRestClientGroupConfigurer(
      ArgoCDConfigurationProperties props,
      ArgoCDServiceAccountPasswordResolver passwordResolver,
      ObjectProvider<ArgoCDAuthQueries> authQueries) {
    this.props = props;
    this.passwordResolver = passwordResolver;
    this.authQueries = authQueries;
  }

  @Override
  public void configureGroups(HttpServiceGroupConfigurer.Groups<RestClient.Builder> groups) {
    groups
        .filterByName("argocd-data")
        .forEachClient(
            (group, clientBuilder) -> {
              AtomicReference<String> sessionToken = new AtomicReference<>();
              clientBuilder.requestInterceptor(
                  (request, body, execution) -> {
                    ArgoCDAuthQueries queries = authQueries.getObject();
                    String password =
                        serviceAccountPassword.updateAndGet(
                            current -> current != null ? current : passwordResolver.resolve(queries));
                    sessionToken.updateAndGet(
                        current ->
                            current != null
                                ? current
                                : queries
                                    .createSessionToken(
                                        new ArgoCdSessionRequest(props.serviceAccountName(), password))
                                    .token());
                    request.getHeaders().setBearerAuth(sessionToken.get());
                    return execution.execute(request, body);
                  });
            });
  }
}
