package com.nilslee.mcp.tools;

import com.nilslee.mcp.service.gitops.ArgoCDGitOpsService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP tools backed by Argo CD ({@code /api/v1/*}). Responses are JSON bodies as text for the model.
 *
 * @see ArgoCDGitOpsService
 */
@Component
public class GitOpsTools {

  private static final Logger log = LoggerFactory.getLogger(GitOpsTools.class);

  private static final String CONTEXT =
      "For k8s-lab GitOps via Argo CD REST API. Returns Argo CD JSON as text. ";

  private final ArgoCDGitOpsService argoCDGitOpsService;

  public GitOpsTools(ArgoCDGitOpsService argoCDGitOpsService) {
    this.argoCDGitOpsService = argoCDGitOpsService;
  }

  /**
   * Lists Argo CD applications with optional filters.
   *
   * @return Argo CD application list JSON as text
   */
  @McpTool(
      name = "argocd-list-applications",
      description =
          CONTEXT
              + "List applications. Use to discover apps, health, sync status, or filter by project/repo. "
              + "Optional refresh can force live state (Argo CD refresh query param).",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listApplications(
      @McpToolParam(required = false, description = "Filter by application metadata name.") @Nullable String name,
      @McpToolParam(
              required = false,
              description = "Argo CD refresh: e.g. normal, hard — forces reconciliation when set.")
          @Nullable
          String refresh,
      @McpToolParam(required = false, description = "Restrict to one or more Argo CD project names.")
          @Nullable
          List<String> projects,
      @McpToolParam(required = false, description = "Kubernetes-style resourceVersion for list consistency.")
          @Nullable
          String resourceVersion,
      @McpToolParam(required = false, description = "Label selector for applications.") @Nullable String selector,
      @McpToolParam(required = false, description = "Filter by Git repository URL.") @Nullable String repo,
      @McpToolParam(
              required = false,
              description = "Application namespace (Argo CD app namespace, not necessarily target cluster NS).")
          @Nullable
          String appNamespace,
      @McpToolParam(required = false, description = "Single project name filter (Argo CD project).") @Nullable
          String project) {
    log.debug("argocd-list-applications name={}", name);
    return argoCDGitOpsService.listApplications(
        name, refresh, projects, resourceVersion, selector, repo, appNamespace, project);
  }

  /**
   * Fetches one Argo CD application by name.
   *
   * @param name application name (required)
   * @return Argo CD application JSON as text
   */
  @McpTool(
      name = "argocd-get-application",
      description =
          CONTEXT + "Get one application by name including status, spec, and operation state.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getApplication(
      @McpToolParam(description = "Application metadata name.") String name,
      @McpToolParam(
              required = false,
              description = "Argo CD refresh: e.g. normal, hard — forces reconciliation when set.")
          @Nullable
          String refresh,
      @McpToolParam(required = false, description = "Restrict listing context to project(s).") @Nullable
          List<String> projects,
      @McpToolParam(required = false, description = "resourceVersion for optimistic concurrency.") @Nullable
          String resourceVersion,
      @McpToolParam(required = false, description = "Label selector.") @Nullable String selector,
      @McpToolParam(required = false, description = "Filter by repo URL.") @Nullable String repo,
      @McpToolParam(required = false, description = "Application namespace.") @Nullable String appNamespace,
      @McpToolParam(required = false, description = "Argo CD project name.") @Nullable String project) {
    log.debug("argocd-get-application name={}", name);
    return argoCDGitOpsService.getApplication(
        name, refresh, projects, resourceVersion, selector, repo, appNamespace, project);
  }

  /**
   * Resource tree and managed resources for an application.
   *
   * @param applicationName Argo CD application name
   * @return Combined text sections for resource tree and managed resources
   */
  @McpTool(
      name = "argocd-list-application-resources",
      description =
          CONTEXT
              + "List live resource tree and managed resources for an application. "
              + "Use for debugging sync, drift, or orphaned resources. Optional filters match Argo CD query params.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listApplicationResources(
      @McpToolParam(description = "Argo CD application name.") String applicationName,
      @McpToolParam(required = false, description = "Filter by live object namespace.") @Nullable String namespace,
      @McpToolParam(required = false, description = "Filter by object name.") @Nullable String name,
      @McpToolParam(required = false, description = "Filter by API version.") @Nullable String version,
      @McpToolParam(required = false, description = "Filter by API group.") @Nullable String group,
      @McpToolParam(required = false, description = "Filter by kind.") @Nullable String kind,
      @McpToolParam(required = false, description = "Application namespace.") @Nullable String appNamespace,
      @McpToolParam(required = false, description = "Argo CD project.") @Nullable String project) {
    log.debug("argocd-list-application-resources app={}", applicationName);
    return argoCDGitOpsService.listApplicationResources(
        applicationName, namespace, name, version, group, kind, appNamespace, project);
  }

  /**
   * Manifests rendered for an application at a revision.
   *
   * @param name application name
   * @return Argo CD manifests response JSON as text
   */
  @McpTool(
      name = "argocd-list-manifests",
      description =
          CONTEXT
              + "List rendered manifests for an application (optionally at a specific revision). "
              + "Use to inspect what Argo CD would apply.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listManifests(
      @McpToolParam(description = "Application metadata name.") String name,
      @McpToolParam(required = false, description = "Git or resolved revision to render.") @Nullable String revision,
      @McpToolParam(required = false, description = "Application namespace.") @Nullable String appNamespace,
      @McpToolParam(required = false, description = "Argo CD project.") @Nullable String project,
      @McpToolParam(required = false, description = "Source positions (multi-source apps).") @Nullable
          List<String> sourcePositions,
      @McpToolParam(required = false, description = "Revisions per source when multi-source.") @Nullable
          List<String> revisions,
      @McpToolParam(required = false, description = "When true, bypass manifest cache.") @Nullable Boolean noCache) {
    log.debug("argocd-list-manifests name={} revision={}", name, revision);
    return argoCDGitOpsService.listManifests(
        name, revision, appNamespace, project, sourcePositions, revisions, noCache);
  }

  /**
   * Argo CD AppProjects.
   *
   * @return Project list JSON as text
   */
  @McpTool(
      name = "argocd-list-projects",
      description = CONTEXT + "List Argo CD AppProjects (optionally filter by project name).",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listProjects(
      @McpToolParam(required = false, description = "Filter by project metadata name.") @Nullable String name) {
    log.debug("argocd-list-projects name={}", name);
    return argoCDGitOpsService.listProjects(name);
  }

  /**
   * Configured Git/Helm/OCI repositories.
   *
   * @return Repository list JSON as text
   */
  @McpTool(
      name = "argocd-list-repositories",
      description =
          CONTEXT
              + "List repository credentials and connection status. "
              + "Optional forceRefresh triggers reconnect (Argo CD forceRefresh param).",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listRepositories(
      @McpToolParam(required = false, description = "Filter by repo URL.") @Nullable String repo,
      @McpToolParam(required = false, description = "Set to refresh repo index/connection.") @Nullable
          String forceRefresh,
      @McpToolParam(required = false, description = "Restrict to repositories allowed for this AppProject.")
          @Nullable
          String appProject) {
    log.debug("argocd-list-repositories repo={}", repo);
    return argoCDGitOpsService.listRepositories(repo, forceRefresh, appProject);
  }
}
