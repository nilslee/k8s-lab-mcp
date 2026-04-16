package com.nilslee.mcp.config.gitops.argocd;

import com.nilslee.mcp.service.gitops.argocd.auth.ArgoCDAuthQueries;
import com.nilslee.mcp.service.gitops.argocd.query.ArgoCDQueries;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Registers Argo CD declarative HTTP clients ({@code argocd-auth}, {@code argocd-data}) and binds
 * {@link ArgoCDConfigurationProperties}.
 *
 * <p><strong>Contract:</strong> Do not inject {@link ArgoCDAuthQueries}, {@link ArgoCDQueries}, or other HTTP
 * service proxies here, and do not call them from {@code @PostConstruct}. That couples this class to
 * {@code httpServiceProxyRegistry} while the registry also consumes {@link org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer}
 * beans (see {@link ArgoCDDataRestClientGroupConfigurer}), which causes a startup cycle. Bearer auth and password
 * bootstrap live on {@link ArgoCDDataRestClientGroupConfigurer} instead; that class defers use of
 * {@link ArgoCDAuthQueries} to a {@link org.springframework.http.client.ClientHttpRequestInterceptor} so proxies are
 * not resolved during registry initialization.
 */
@Configuration
@EnableConfigurationProperties(ArgoCDConfigurationProperties.class)
@ImportHttpServices(group = "argocd-auth", types = ArgoCDAuthQueries.class)
@ImportHttpServices(group = "argocd-data", types = ArgoCDQueries.class)
public class ArgoCDHttpServicesConfiguration {}
