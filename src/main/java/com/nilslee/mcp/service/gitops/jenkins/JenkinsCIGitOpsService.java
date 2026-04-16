package com.nilslee.mcp.service.gitops.jenkins;

import com.nilslee.mcp.config.gitops.jenkins.JenkinsConfigurationProperties;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsAgentQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsBuildQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsConsoleQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsJobQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsPluginQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsQueueExecutorQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsSystemQueries;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Aggregates read-only Jenkins REST diagnostics into plain-text sections for MCP tools.
 *
 * <p>Loaded when {@code mcp.gitops.jenkins.enabled=true}. Depends on {@link org.springframework.web.service.annotation.HttpExchange}
 * clients registered for the {@code jenkins} HTTP service client group.
 *
 * <p>Console log methods honor {@link JenkinsConfigurationProperties#maxLogChars()} and
 * {@link JenkinsConfigurationProperties#maxProgressiveChunks()} to avoid unbounded responses and non-advancing
 * progressive streams.
 */
@Service
@ConditionalOnProperty(prefix = "mcp.gitops.jenkins", name = "enabled", havingValue = "true")
public class JenkinsCIGitOpsService {

  private static final int MAX_TAIL_LINES = 50_000;

  private final JenkinsAgentQueries jenkinsAgentQueries;
  private final JenkinsBuildQueries jenkinsBuildQueries;
  private final JenkinsConsoleQueries jenkinsConsoleQueries;
  private final JenkinsJobQueries jenkinsJobQueries;
  private final JenkinsPluginQueries jenkinsPluginQueries;
  private final JenkinsQueueExecutorQueries jenkinsQueueExecutorQueries;
  private final JenkinsSystemQueries jenkinsSystemQueries;
  private final JenkinsConfigurationProperties jenkinsProperties;

  public JenkinsCIGitOpsService(
      JenkinsAgentQueries jenkinsAgentQueries,
      JenkinsBuildQueries jenkinsBuildQueries,
      JenkinsConsoleQueries jenkinsConsoleQueries,
      JenkinsJobQueries jenkinsJobQueries,
      JenkinsPluginQueries jenkinsPluginQueries,
      JenkinsQueueExecutorQueries jenkinsQueueExecutorQueries,
      JenkinsSystemQueries jenkinsSystemQueries,
      JenkinsConfigurationProperties jenkinsProperties) {
    this.jenkinsAgentQueries = jenkinsAgentQueries;
    this.jenkinsBuildQueries = jenkinsBuildQueries;
    this.jenkinsConsoleQueries = jenkinsConsoleQueries;
    this.jenkinsJobQueries = jenkinsJobQueries;
    this.jenkinsPluginQueries = jenkinsPluginQueries;
    this.jenkinsQueueExecutorQueries = jenkinsQueueExecutorQueries;
    this.jenkinsSystemQueries = jenkinsSystemQueries;
    this.jenkinsProperties = jenkinsProperties;
  }

  /**
   * Controller version, quieting, readiness-style JSON, and overall readiness summary.
   *
   * @return concatenated plain-text sections for the model
   */
  public String getSystemSummary() {
    String systemInformation = jenkinsSystemQueries.getJenkinsSystemInfo();
    String readiness = jenkinsSystemQueries.getOverallReadiness();
    return "System Information: \n"
        + systemInformation
        + "\n --- \nReadiness: \n"
        + readiness;
  }

  /** Shallow job tree from {@code /api/json?tree=jobs[...]}. */
  public String listJobs() {
    return jenkinsJobQueries.getAllJobs();
  }

  /**
   * One job: health and last build ref, buildable/SCM summary, and parameter/property definitions.
   *
   * @param jobName job name or folder path as in Jenkins URLs
   */
  public String getJobDetails(String jobName) {
    String healthAndLastBuildRef = jenkinsJobQueries.getJobHealthReportAndLastBuildRef(jobName);
    String buildableAndScm = jenkinsJobQueries.getBuildableAndScm(jobName);
    String jobProperties = jenkinsJobQueries.getJobProperty(jobName);

    return "Health and Last Build Reference:\n"
        + healthAndLastBuildRef
        + "\n---\nBuildable and SCM:\n"
        + buildableAndScm
        + "\n---\nJob Properties:\n"
        + jobProperties;
  }

  /**
   * Last build: metadata, causes, parameters, change sets, artifacts, test report, and workflow status.
   *
   * @param jobName job name or folder path
   */
  public String getLastBuild(String jobName) {
    String metadata = jenkinsBuildQueries.getBuildMetadata(jobName);
    String trigger = jenkinsBuildQueries.getBuildTrigger(jobName);
    String params = jenkinsBuildQueries.getBuildParameters(jobName);
    String commitChangeSets = jenkinsBuildQueries.getCommitChangeSets(jobName);
    String artifacts = jenkinsBuildQueries.getArtifacts(jobName);
    String testReport = jenkinsBuildQueries.getTestReport(jobName);
    String pipelineStatus = jenkinsBuildQueries.getPipelineStatus(jobName);

    return "Build Metadata:\n"
        + metadata
        + "\n---\nBuild Trigger:\n"
        + trigger
        + "\n---\nBuild Parameters:\n"
        + params
        + "\n---\nBuild Commit Change Sets:\n"
        + commitChangeSets
        + "\n---\nBuild Artifacts:\n"
        + artifacts
        + "\n---\nBuild Test Report:\n"
        + testReport
        + "\n---\nBuild Pipeline Status:\n"
        + pipelineStatus;
  }

  /**
   * Specific build by number: same sections as {@link #getLastBuild(String)}.
   *
   * @param jobName job name or folder path
   * @param buildNumber Jenkins build number
   */
  public String getBuild(String jobName, Integer buildNumber) {
    String metadata = jenkinsBuildQueries.getBuildMetadata(jobName, buildNumber);
    String trigger = jenkinsBuildQueries.getBuildTrigger(jobName, buildNumber);
    String params = jenkinsBuildQueries.getBuildParameters(jobName, buildNumber);
    String commitChangeSets = jenkinsBuildQueries.getCommitChangeSets(jobName, buildNumber);
    String artifacts = jenkinsBuildQueries.getArtifacts(jobName, buildNumber);
    String testReport = jenkinsBuildQueries.getTestReport(jobName, buildNumber);
    String pipelineStatus = jenkinsBuildQueries.getPipelineStatus(jobName, buildNumber);

    return "Build Metadata:\n"
        + metadata
        + "\n---\nBuild Trigger:\n"
        + trigger
        + "\n---\nBuild Parameters:\n"
        + params
        + "\n---\nBuild Commit Change Sets:\n"
        + commitChangeSets
        + "\n---\nBuild Artifacts:\n"
        + artifacts
        + "\n---\nBuild Test Report:\n"
        + testReport
        + "\n---\nBuild Pipeline Status:\n"
        + pipelineStatus;
  }

  /**
   * Full console log for the job's last build via progressiveText until complete or until caps apply.
   *
   * @param jobName job name or folder path
   * @return concatenated log text, possibly with a trailing truncation notice
   */
  public String getBuildLogs(String jobName) {
    return readFullProgressive(offset -> jenkinsConsoleQueries.getProgressiveConsoleText(jobName, offset));
  }

  /**
   * Full console log for a numbered build (same progressive semantics as {@link #getBuildLogs(String)}).
   *
   * @param jobName job name or folder path
   * @param buildNumber build number
   */
  public String getBuildLogs(String jobName, Integer buildNumber) {
    return readFullProgressive(
        offset -> jenkinsConsoleQueries.getProgressiveConsoleText(jobName, buildNumber, offset));
  }

  /**
   * Last {@code lines} complete lines of console output for the last build, streaming progressive chunks without
   * materializing the full log when possible.
   *
   * @param jobName job name or folder path
   * @param lines number of lines to retain from the end (defaults to 100 if null or non-positive)
   */
  public String getBuildLogTail(String jobName, Integer lines) {
    int n = normalizeTailLineCount(lines);
    return readTailProgressive(
        offset -> jenkinsConsoleQueries.getProgressiveConsoleText(jobName, offset), n);
  }

  /**
   * Last {@code lines} lines for a specific build.
   *
   * @param jobName job name or folder path
   * @param buildNumber build number
   * @param lines number of lines to retain from the end
   */
  public String getBuildLogTail(String jobName, Integer buildNumber, Integer lines) {
    int n = normalizeTailLineCount(lines);
    return readTailProgressive(
        offset -> jenkinsConsoleQueries.getProgressiveConsoleText(jobName, buildNumber, offset), n);
  }

  /** Queue items, label assignment hints, and computer executor snapshot. */
  public String getQueueInfo() {
    String queueStatus = jenkinsQueueExecutorQueries.getQueueStatus();
    String queueLabelAndAssigned = jenkinsQueueExecutorQueries.getQueueLabelAndAssigned();
    String computerStatus = jenkinsQueueExecutorQueries.getComputerStatus();

    return "Queue Status:\n"
        + queueStatus
        + "\n---\nQueue Label and Assigned Node:\n"
        + queueLabelAndAssigned
        + "\n---\nExecutor Snapshot:\n"
        + computerStatus;
  }

  /** Summary of all computers/agents. */
  public String getAgentComputerSummary() {
    return jenkinsAgentQueries.getAgentComputers();
  }

  /**
   * Executor snapshot for one computer.
   *
   * @param computerName computer display name
   */
  public String getAgentComputerDetail(String computerName) {
    return jenkinsAgentQueries.getAgentExecutors(computerName);
  }

  /** Installed plugins and update-center available updates. */
  public String getPluginInfo() {
    String plugins = jenkinsPluginQueries.getPlugins();
    String availableUpdates = jenkinsPluginQueries.getAvailableUpdates();

    return "Plugin Information:\n"
        + plugins
        + "\n---\nAvailable Updates:\n"
        + availableUpdates;
  }

  @FunctionalInterface
  private interface ProgressiveChunkFetcher {
    ResponseEntity<String> fetch(int offset);
  }

  private String readFullProgressive(ProgressiveChunkFetcher fetcher) {
    int maxChars = jenkinsProperties.maxLogChars();
    int maxChunks = jenkinsProperties.maxProgressiveChunks();
    StringBuilder out = new StringBuilder(Math.min(maxChars, 4096));
    int offset = 0;
    for (int i = 0; i < maxChunks; i++) {
      ResponseEntity<String> response = fetcher.fetch(offset);
      String chunk = progressiveBody(response);
      int room = maxChars - out.length();
      if (room <= 0) {
        out.append("\n...[log truncated by maxLogChars]");
        return out.toString();
      }
      if (chunk.length() > room) {
        out.append(chunk, 0, room);
        out.append("\n...[log truncated by maxLogChars]");
        return out.toString();
      }
      out.append(chunk);
      if (!moreData(response)) {
        return out.toString();
      }
      int next = parseTextSize(response.getHeaders(), offset);
      if (next <= offset) {
        out.append("\n...[progressive console stopped: non-increasing X-Text-Size]");
        return out.toString();
      }
      offset = next;
    }
    out.append("\n...[log truncated by maxProgressiveChunks]");
    return out.toString();
  }

  private String readTailProgressive(ProgressiveChunkFetcher fetcher, int maxLines) {
    int maxChars = jenkinsProperties.maxLogChars();
    int maxChunks = jenkinsProperties.maxProgressiveChunks();
    Deque<String> tail = new ArrayDeque<>(Math.min(maxLines, 256));
    StringBuilder pending = new StringBuilder();
    int offset = 0;
    int rawBytes = 0;
    for (int i = 0; i < maxChunks; i++) {
      ResponseEntity<String> response = fetcher.fetch(offset);
      String chunk = progressiveBody(response);
      if (rawBytes + chunk.length() > maxChars) {
        int allowed = maxChars - rawBytes;
        if (allowed > 0) {
          pending.append(chunk, 0, allowed);
        }
        flushCompleteLines(pending, tail, maxLines);
        if (!pending.isEmpty()) {
          addTailLine(tail, maxLines, pending.toString());
          pending.setLength(0);
        }
        tail.addLast("...[log tail scan truncated by maxLogChars]");
        break;
      }
      rawBytes += chunk.length();
      pending.append(chunk);
      flushCompleteLines(pending, tail, maxLines);
      if (!moreData(response)) {
        break;
      }
      int next = parseTextSize(response.getHeaders(), offset);
      if (next <= offset) {
        break;
      }
      offset = next;
    }
    if (!pending.isEmpty()) {
      addTailLine(tail, maxLines, pending.toString());
    }
    return String.join("\n", tail);
  }

  private static void flushCompleteLines(StringBuilder pending, Deque<String> tail, int maxLines) {
    int nl;
    while ((nl = indexOfNewline(pending)) >= 0) {
      String line = pending.substring(0, nl);
      pending.delete(0, nl + 1);
      addTailLine(tail, maxLines, line);
    }
  }

  private static int indexOfNewline(StringBuilder sb) {
    for (int i = 0; i < sb.length(); i++) {
      if (sb.charAt(i) == '\n') {
        return i;
      }
    }
    return -1;
  }

  private static void addTailLine(Deque<String> tail, int maxLines, String line) {
    if (tail.size() == maxLines) {
      tail.removeFirst();
    }
    tail.addLast(line);
  }

  private static int normalizeTailLineCount(Integer lines) {
    if (lines == null || lines < 1) {
      return 100;
    }
    return Math.min(lines, MAX_TAIL_LINES);
  }

  private static String progressiveBody(ResponseEntity<String> response) {
    String body = response.getBody();
    return body != null ? body : "";
  }

  private static boolean moreData(ResponseEntity<String> response) {
    return Optional.ofNullable(response.getHeaders().getFirst("X-More-Data"))
        .map(v -> v.equalsIgnoreCase("yes"))
        .orElse(false);
  }

  private static int parseTextSize(HttpHeaders headers, int currentOffset) {
    String raw = headers.getFirst("X-Text-Size");
    if (raw == null) {
      return currentOffset;
    }
    try {
      return Integer.parseInt(raw);
    } catch (NumberFormatException ignored) {
      return currentOffset;
    }
  }
}
