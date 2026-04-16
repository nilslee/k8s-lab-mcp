package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface AgentQueries {

  @GetExchange("/computer/api/json?tree=computer[assignedLabels[name],displayName,offline,offlineCauseReason,temporarilyOffline,monitorData,hudson.node_monitors.*]")
  String getAgentComputers();

  @GetExchange("/computer/({name})/api/json?tree=executors[currentExecutable[url]],oneOffExecutors[currentExecutable[url]]")
  String getAgentExecutors(
      @PathVariable String name);
}
