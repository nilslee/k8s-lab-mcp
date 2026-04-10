package com.nilslee.mcp.config.gitops;

import com.nilslee.mcp.service.gitops.auth.ArgoCDAuthQueries;
import com.nilslee.mcp.service.gitops.auth.ArgoCDServiceAccountPasswordResolver;
import com.nilslee.mcp.service.gitops.auth.ArgoCDSessionCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroupConfigurer;

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
  private final ArgoCDSessionCache sessionCache;
  private final ObjectProvider<ArgoCDAuthQueries> authQueries;
  private final AtomicReference<String> serviceAccountPassword = new AtomicReference<>();

  public ArgoCDDataRestClientGroupConfigurer(
      ArgoCDConfigurationProperties props,
      ArgoCDServiceAccountPasswordResolver passwordResolver,
      ArgoCDSessionCache sessionCache,
      ObjectProvider<ArgoCDAuthQueries> authQueries) {
    this.props = props;
    this.passwordResolver = passwordResolver;
    this.sessionCache = sessionCache;
    this.authQueries = authQueries;
  }

  @Override
  public void configureGroups(HttpServiceGroupConfigurer.Groups<RestClient.Builder> groups) {
    groups
        .filterByName("argocd-data")
        .forEachClient(
            (group, clientBuilder) ->
                clientBuilder.requestInterceptor(
                    (request, body, execution) -> {
                      ArgoCDAuthQueries queries = authQueries.getObject();
                      String password =
                          serviceAccountPassword.updateAndGet(
                              current -> current != null ? current : passwordResolver.resolve(queries));
                      String token =
                          sessionCache.getOrCreateSessionToken(
                              queries, props.serviceAccountName(), password);
                      request.getHeaders().setBearerAuth(token);
                      return execution.execute(request, body);
                    }));
  }
}
