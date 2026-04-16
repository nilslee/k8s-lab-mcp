package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Jenkins computers (controller and agents): labels, offline state, and per-node executors.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 */
@HttpExchange
public interface JenkinsAgentQueries {

  /**
   * All computers with assigned labels, offline flags, and node monitor snapshots.
   *
   * @see <a href="https://www.jenkins.io/doc/book/using/remote-access-api/">Jenkins remote API</a>
   */
  @GetExchange(
      "/computer/api/json?tree=computer[assignedLabels[name],displayName,offline,offlineCauseReason,temporarilyOffline,monitorData,hudson.node_monitors.*]")
  String getAgentComputers();

  /**
   * Executors and current work URLs for one computer (use display name from {@link #getAgentComputers()}).
   *
   * @param computerName Jenkins computer name as used in {@code /computer/{name}/...} URLs
   */
  @GetExchange(
      "/computer/{computerName}/api/json?tree=executors[currentExecutable[url]],oneOffExecutors[currentExecutable[url]]")
  String getAgentExecutors(@PathVariable String computerName);
}
