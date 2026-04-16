package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Job discovery (shallow tree) and per-job health, SCM, and parameter definitions.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 */
@HttpExchange
public interface JenkinsJobQueries {

  /** Root-level jobs with nested {@code jobs} for folders (shallow tree). */
  @GetExchange("/api/json?tree=jobs[name,fullName,url,_class,jobs]")
  String getAllJobs();

  /** Health report and last build pointer for one job. */
  @GetExchange("/job/{jobName}/api/json?tree=healthReport,lastBuild[number,result,url]")
  String getJobHealthReportAndLastBuildRef(@PathVariable String jobName);

  /** Whether the job is buildable and configured SCM remote URLs. */
  @GetExchange("/job/{jobName}/api/json?tree=buildable,scm[userRemoteConfigs[url]]")
  String getBuildableAndScm(@PathVariable String jobName);

  /** Job properties including parameter definitions (name, type, default). */
  @GetExchange(
      "/job/{jobName}/api/json?tree=property[parameterDefinitions[name,type,defaultParameterValue]]")
  String getJobProperty(@PathVariable String jobName);
}
