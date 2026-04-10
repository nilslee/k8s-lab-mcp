package com.nilslee.mcp.service.gitops;

import com.nilslee.mcp.service.gitops.auth.ArgoCDSessionCache;
import com.nilslee.mcp.service.gitops.query.ArgoCDQueries;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Application service for Argo CD read operations used by {@link com.nilslee.mcp.tools.GitOpsTools}.
 * Delegates to {@link ArgoCDQueries}; combines resource tree and managed resources for one MCP-facing call.
 */
@Service
public class ArgoCDGitOpsService {

  private final ArgoCDQueries argoCDQueries;
  private final ArgoCDSessionCache sessionCache;

  public ArgoCDGitOpsService(ArgoCDQueries argoCDQueries, ArgoCDSessionCache sessionCache) {
    this.argoCDQueries = argoCDQueries;
    this.sessionCache = sessionCache;
  }

  /**
   * Clears the cached Argo CD session and retries once on HTTP 401 so a fresh {@code POST /session} runs on the next
   * outbound request (handles server restarts and expired JWTs).
   */
  private String with401Retry(Supplier<String> supplier) {
    try {
      return supplier.get();
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().value() == 401) {
        sessionCache.invalidateSession();
        return supplier.get();
      }
      throw e;
    }
  }

  /**
   * {@code GET /api/v1/applications} with optional query filters.
   *
   * @return JSON response body as text
   */
  public String listApplications(
      @Nullable String name,
      @Nullable String refresh,
      @Nullable List<String> projects,
      @Nullable String resourceVersion,
      @Nullable String selector,
      @Nullable String repo,
      @Nullable String appNamespace,
      @Nullable String project) {
    return with401Retry(
        () ->
            argoCDQueries.listApplications(
                name, refresh, projects, resourceVersion, selector, repo, appNamespace, project));
  }

  /**
   * {@code GET /api/v1/applications/{name}}.
   *
   * @return JSON response body as text
   */
  public String getApplication(
      String name,
      @Nullable String refresh,
      @Nullable List<String> projects,
      @Nullable String resourceVersion,
      @Nullable String selector,
      @Nullable String repo,
      @Nullable String appNamespace,
      @Nullable String project) {
    return with401Retry(
        () ->
            argoCDQueries.getApplication(
                name, refresh, projects, resourceVersion, selector, repo, appNamespace, project));
  }

  /**
   * Fetches {@code GET .../resource-tree} and {@code GET .../managed-resources}, then concatenates both
   * into one string for the model.
   *
   * @return formatted text with two sections
   */
  public String listApplicationResources(
      String applicationName,
      @Nullable String namespace,
      @Nullable String name,
      @Nullable String version,
      @Nullable String group,
      @Nullable String kind,
      @Nullable String appNamespace,
      @Nullable String project) {
    return with401Retry(
        () -> {
          String resourceTree =
              argoCDQueries.getResourceTree(
                  applicationName, namespace, name, version, group, kind, appNamespace, project);
          String managedResources =
              argoCDQueries.getManagedResources(
                  applicationName, namespace, name, version, group, kind, appNamespace, project);
          return "Resource Tree: \n"
              + resourceTree
              + "\n---\nManaged Resources: \n"
              + managedResources;
        });
  }

  /** {@code GET /api/v1/applications/{name}/manifests}. */
  public String listManifests(
      String name,
      @Nullable String revision,
      @Nullable String appNamespace,
      @Nullable String project,
      @Nullable List<String> sourcePositions,
      @Nullable List<String> revisions,
      @Nullable Boolean noCache) {
    return with401Retry(
        () ->
            argoCDQueries.getManifests(
                name, revision, appNamespace, project, sourcePositions, revisions, noCache));
  }

  /** {@code GET /api/v1/projects}. */
  public String listProjects(@Nullable String name) {
    return with401Retry(() -> argoCDQueries.listProjects(name));
  }

  /** {@code GET /api/v1/repositories}. */
  public String listRepositories(
      @Nullable String repo, @Nullable String forceRefresh, @Nullable String appProject) {
    return with401Retry(() -> argoCDQueries.listRepositories(repo, forceRefresh, appProject));
  }
}
