package com.nilslee.mcp.config.gitops.jenkins;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * MCP feature flags and Jenkins console safeguards under prefix {@code mcp.gitops.jenkins}.
 *
 * <p>Base URL, connect timeout, and read timeout for Jenkins REST calls are configured separately on the
 * {@code spring.http.serviceclient.jenkins} HTTP service client (same {@code jenkins} group name as
 * {@link JenkinsHttpRestClientConfiguration}).
 *
 * <p>{@link com.nilslee.mcp.service.gitops.jenkins.JenkinsCIGitOpsService} and
 * {@link com.nilslee.mcp.tools.gitops.JenkinsTools} load only when {@code mcp.gitops.jenkins.enabled=true}
 * ({@code @ConditionalOnProperty}).
 */
@ConfigurationProperties("mcp.gitops.jenkins")
public record JenkinsConfigurationProperties(
    @DefaultValue("false") boolean enabled,

    @DefaultValue("admin") String username,
    @DefaultValue("admin") String password,

    /** Maximum characters accumulated from progressive console before truncation. */
    @DefaultValue("1048576") int maxLogChars,
    /** Maximum progressiveText HTTP round-trips per log fetch (guards stuck {@code X-More-Data}). */
    @DefaultValue("5000") int maxProgressiveChunks) {}
