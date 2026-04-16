/**
 * Jenkins GitOps integration for MCP: HTTP service client registration and typed configuration.
 *
 * <p><strong>Runtime contract:</strong> {@link JenkinsHttpRestClientConfiguration} registers
 * {@link org.springframework.web.service.annotation.HttpExchange} proxies against the {@code jenkins} client group
 * ({@code spring.http.serviceclient.jenkins}). {@link JenkinsConfigurationProperties} binds {@code mcp.gitops.jenkins.*}
 * (feature flag, console safeguards). Jenkins MCP tools and {@link com.nilslee.mcp.service.gitops.jenkins.JenkinsCIGitOpsService}
 * load only when {@code mcp.gitops.jenkins.enabled=true}.
 */
package com.nilslee.mcp.config.gitops.jenkins;
