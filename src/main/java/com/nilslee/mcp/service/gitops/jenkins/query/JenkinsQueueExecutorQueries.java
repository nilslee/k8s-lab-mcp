package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Build queue items and executor/computer utilization.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 */
@HttpExchange
public interface JenkinsQueueExecutorQueries {

  /** Queued tasks with human-readable {@code why} and stuck flag. */
  @GetExchange("/queue/api/json?tree=items[task[name,url],why,stuck]")
  String getQueueStatus();

  /** Queue items including assigned label when present. */
  @GetExchange("/queue/api/json?tree=items[task[name,url],assignedLabel[name],why,stuck]")
  String getQueueLabelAndAssigned();

  /** Per-computer executor counts and offline flags. */
  @GetExchange(
      "/computer/api/json?tree=computer[displayName,offline,temporarilyOffline,numExecutors,busyExecutors]")
  String getComputerStatus();
}
