package com.nilslee.mcp.service.gitops.argocd.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * Declarative HTTP client for Argo CD server {@code /api/v1} GET APIs. Uses bearer session token
 * configured on the {@code argocd-data} RestClient group.
 */
@HttpExchange("/api/v1")
public interface ArgoCDQueries {

  /** List applications; query params match Argo CD’s application list filters. */
  @GetExchange("/applications")
  String listApplications(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String refresh,
      @RequestParam(required = false) List<String> projects,
      @RequestParam(required = false) String resourceVersion,
      @RequestParam(required = false) String selector,
      @RequestParam(required = false) String repo,
      @RequestParam(required = false) String appNamespace,
      @RequestParam(required = false) String project);

  /** Get one application by path {@code name}. */
  @GetExchange("/applications/{name}")
  String getApplication(
      @PathVariable String name,
      @RequestParam(required = false) String refresh,
      @RequestParam(required = false) List<String> projects,
      @RequestParam(required = false) String resourceVersion,
      @RequestParam(required = false) String selector,
      @RequestParam(required = false) String repo,
      @RequestParam(required = false) String appNamespace,
      @RequestParam(required = false) String project);

  /** Managed (tracked) resources for an application. */
  @GetExchange("/applications/{applicationName}/managed-resources")
  String getManagedResources(
      @PathVariable String applicationName,
      @RequestParam(required = false) String namespace,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String version,
      @RequestParam(required = false) String group,
      @RequestParam(required = false) String kind,
      @RequestParam(required = false) String appNamespace,
      @RequestParam(required = false) String project);

  /** Live resource tree for an application. */
  @GetExchange("/applications/{applicationName}/resource-tree")
  String getResourceTree(
      @PathVariable String applicationName,
      @RequestParam(required = false) String namespace,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String version,
      @RequestParam(required = false) String group,
      @RequestParam(required = false) String kind,
      @RequestParam(required = false) String appNamespace,
      @RequestParam(required = false) String project);

  /** Rendered manifests for an application. */
  @GetExchange("/applications/{name}/manifests")
  String getManifests(
      @PathVariable String name,
      @RequestParam(required = false) String revision,
      @RequestParam(required = false) String appNamespace,
      @RequestParam(required = false) String project,
      @RequestParam(required = false) List<String> sourcePositions,
      @RequestParam(required = false) List<String> revisions,
      @RequestParam(required = false) Boolean noCache);

  /** AppProject list. */
  @GetExchange("/projects")
  String listProjects(@RequestParam(required = false) String name);

  /** Configured repositories. */
  @GetExchange("/repositories")
  String listRepositories(
      @RequestParam(required = false) String repo,
      @RequestParam(required = false) String forceRefresh,
      @RequestParam(required = false) String appProject);
}
