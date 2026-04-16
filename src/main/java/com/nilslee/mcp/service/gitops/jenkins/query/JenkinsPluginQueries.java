package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Installed plugins and update-center metadata.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 */
@HttpExchange
public interface JenkinsPluginQueries {

  /** Installed plugins with shallow dependency depth. */
  @GetExchange("/pluginManager/api/json?depth=1")
  String getPlugins();

  /** Update center JSON (available updates, etc.). */
  @GetExchange("/updateCenter/api/json")
  String getAvailableUpdates();
}
