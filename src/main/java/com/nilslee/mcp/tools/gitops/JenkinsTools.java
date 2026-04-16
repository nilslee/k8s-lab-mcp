package com.nilslee.mcp.tools.gitops;

import com.nilslee.mcp.service.gitops.jenkins.JenkinsCIGitOpsService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP tools backed by Jenkins REST diagnostics ({@code /api/json}, progressive console, etc.).
 * Responses are plain-text sections for the model.
 *
 * @see JenkinsCIGitOpsService
 */
@Component
//@ConditionalOnProperty(prefix = "mcp.gitops.jenkins", name = "enabled", havingValue = "true")
public class JenkinsTools {

  private static final Logger log = LoggerFactory.getLogger(JenkinsTools.class);

  private static final String CONTEXT =
      "For k8s-lab Jenkins CI diagnostics via REST. Read-only; returns aggregated plain text (not raw JSON). ";

  private final JenkinsCIGitOpsService jenkinsCIGitOpsService;

  public JenkinsTools(JenkinsCIGitOpsService jenkinsCIGitOpsService) {
    this.jenkinsCIGitOpsService = jenkinsCIGitOpsService;
  }

  @McpTool(
      name = "jenkins-get-system-summary",
      description =
          CONTEXT
              + "Controller version, quieting/readiness-style signals. Use first for 'is Jenkins healthy?' before drilling into jobs.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getSystemSummary() {
    log.debug("jenkins-get-system-summary");
    return jenkinsCIGitOpsService.getSystemSummary();
  }

  @McpTool(
      name = "jenkins-list-jobs",
      description =
          CONTEXT
              + "Shallow job tree/names. Use to discover job paths before calling job- or build-specific tools.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String listJobs() {
    log.debug("jenkins-list-jobs");
    return jenkinsCIGitOpsService.listJobs();
  }

  @McpTool(
      name = "jenkins-get-job-details",
      description =
          CONTEXT
              + "One job: health/last build refs, buildable/SCM summary, parameters/properties. Use after resolving the job name from list-jobs.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getJobDetails(@McpToolParam(description = "Jenkins job name or folder path as used in job URLs.") String jobName) {
    log.debug("jenkins-get-job-details jobName={}", jobName);
    return jenkinsCIGitOpsService.getJobDetails(jobName);
  }

  @McpTool(
      name = "jenkins-get-last-build",
      description =
          CONTEXT
              + "Last completed/latest build for a job: metadata, causes, parameters, changes, artifacts, tests, pipeline/wf summary. Use for 'what failed last?' without a build number.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getLastBuild(@McpToolParam(description = "Jenkins job name or folder path.") String jobName) {
    log.debug("jenkins-get-last-build jobName={}", jobName);
    return jenkinsCIGitOpsService.getLastBuild(jobName);
  }

  @McpTool(
      name = "jenkins-get-build",
      description =
          CONTEXT
              + "Specific build by number (same sections as last-build). Use when the user names a build or you need history beyond lastBuild.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getBuild(
      @McpToolParam(description = "Jenkins job name or folder path.") String jobName,
      @McpToolParam(description = "Build number (integer).") Integer buildNumber) {
    log.debug("jenkins-get-build jobName={} buildNumber={}", jobName, buildNumber);
    return jenkinsCIGitOpsService.getBuild(jobName, buildNumber);
  }

  @McpTool(
      name = "jenkins-get-build-logs",
      description =
          CONTEXT
              + "Full console log via progressiveText until complete. Responses can be very large; prefer jenkins-get-build-log-tail when you only need the failure context. "
              + "Omit buildNumber to use the job's last build.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getBuildLogs(
      @McpToolParam(description = "Jenkins job name or folder path.") String jobName,
      @McpToolParam(
              required = false,
              description = "Build number; when omitted, fetches the last build's console output.")
          @Nullable
          Integer buildNumber) {
    log.debug("jenkins-get-build-logs jobName={} buildNumber={}", jobName, buildNumber);
    if (buildNumber == null) {
      return jenkinsCIGitOpsService.getBuildLogs(jobName);
    }
    return jenkinsCIGitOpsService.getBuildLogs(jobName, buildNumber);
  }

  @McpTool(
      name = "jenkins-get-build-log-tail",
      description =
          CONTEXT
              + "Last N lines of console output (streams progressiveText and keeps only the tail; capped by mcp.gitops.jenkins max-log settings). Safer than full logs for large builds. "
              + "Omit buildNumber to tail the last build.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getBuildLogTail(
      @McpToolParam(description = "Jenkins job name or folder path.") String jobName,
      @McpToolParam(description = "Number of lines to keep from the end of the log.") Integer lines,
      @McpToolParam(
              required = false,
              description = "Build number; when omitted, uses the last build.")
          @Nullable
          Integer buildNumber) {
    log.debug("jenkins-get-build-log-tail jobName={} buildNumber={} lines={}", jobName, buildNumber, lines);
    if (buildNumber == null) {
      return jenkinsCIGitOpsService.getBuildLogTail(jobName, lines);
    }
    return jenkinsCIGitOpsService.getBuildLogTail(jobName, buildNumber, lines);
  }

  @McpTool(
      name = "jenkins-get-queue-info",
      description =
          CONTEXT
              + "Queue items, label/assigned node hints, and executor/computer snapshot. Use for 'why is my build not starting?'.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getQueueInfo() {
    log.debug("jenkins-get-queue-info");
    return jenkinsCIGitOpsService.getQueueInfo();
  }

  @McpTool(
      name = "jenkins-get-agent-computer-summary",
      description =
          CONTEXT
              + "Summary of all computers/agents (controller + nodes). Use for fleet online/offline and capacity at a glance.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getAgentComputerSummary() {
    log.debug("jenkins-get-agent-computer-summary");
    return jenkinsCIGitOpsService.getAgentComputerSummary();
  }

  @McpTool(
      name = "jenkins-get-agent-computer-detail",
      description =
          CONTEXT
              + "Per-computer executors and current work for one agent or 'master'. Use after summary when a specific node is misbehaving.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getAgentComputerDetail(
      @McpToolParam(description = "Computer display name as in Jenkins (e.g. built-in node or agent name).") String computerName) {
    log.debug("jenkins-get-agent-computer-detail computerName={}", computerName);
    return jenkinsCIGitOpsService.getAgentComputerDetail(computerName);
  }

  @McpTool(
      name = "jenkins-get-plugin-info",
      description =
          CONTEXT
              + "Installed plugins and available updates. Use for startup/readiness issues tied to missing or outdated plugins.",
      annotations = @McpAnnotations(readOnlyHint = true, destructiveHint = false))
  public String getPluginInfo() {
    log.debug("jenkins-get-plugin-info");
    return jenkinsCIGitOpsService.getPluginInfo();
  }
}
