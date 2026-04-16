package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Build metadata, causes, parameters, SCM change sets, artifacts, tests, and Pipeline {@code wfapi} status.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 *
 * <p>Some endpoints (e.g. test report, workflow describe) return 404 when not applicable to the build type.
 */
@HttpExchange
public interface JenkinsBuildQueries {

  @GetExchange("/job/{jobName}/lastBuild/api/json")
  String getBuildMetadata(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/api/json")
  String getBuildMetadata(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange(
      "/job/{jobName}/lastBuild/api/json?tree=actions[causes[shortDescription,userName,upstreamProject,upstreamBuild]]")
  String getBuildTrigger(@PathVariable String jobName);

  @GetExchange(
      "/job/{jobName}/{buildNumber}/api/json?tree=actions[causes[shortDescription,userName,upstreamProject,upstreamBuild]]")
  String getBuildTrigger(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange("/job/{jobName}/lastBuild/api/json?tree=actions[parameters[name,value]]")
  String getBuildParameters(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/api/json?tree=actions[parameters[name,value]]")
  String getBuildParameters(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange("/job/{jobName}/lastBuild/api/json?tree=changeSets[items[commitId,msg,author[fullName]]]")
  String getCommitChangeSets(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/api/json?tree=changeSets[items[commitId,msg,author[fullName]]]")
  String getCommitChangeSets(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange("/job/{jobName}/lastBuild/api/json?tree=artifacts[fileName,relativePath]")
  String getArtifacts(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/api/json?tree=artifacts[fileName,relativePath]")
  String getArtifacts(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange("/job/{jobName}/lastBuild/testReport/api/json")
  String getTestReport(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/testReport/api/json")
  String getTestReport(@PathVariable String jobName, @PathVariable Integer buildNumber);

  @GetExchange("/job/{jobName}/lastBuild/wfapi/describe")
  String getPipelineStatus(@PathVariable String jobName);

  @GetExchange("/job/{jobName}/{buildNumber}/wfapi/describe")
  String getPipelineStatus(@PathVariable String jobName, @PathVariable Integer buildNumber);
}
