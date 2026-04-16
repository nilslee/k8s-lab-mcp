package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface PluginQueries {

  @GetExchange("/pluginManager/api/json?depth=1")
  String getPlugins(
      @PathVariable String name);

  @GetExchange("/updateCenter/api/json")
  String getAvailableUpdates(
      @PathVariable String name);
}
