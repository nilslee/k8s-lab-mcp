package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Jenkins controller readiness and high-level mode signals under {@code /api}.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 */
@HttpExchange("/api")
public interface JenkinsSystemQueries {

  /** Overall readiness / plugin issues from {@code GET /api/json}. */
  @GetExchange("/json")
  String getOverallReadiness();

  /** Version, quieting, CSRF flag from {@code GET /api/json?tree=...}. */
  @GetExchange("/json?tree=mode,quietingDown,useCrumbs")
  String getJenkinsSystemInfo();
}
