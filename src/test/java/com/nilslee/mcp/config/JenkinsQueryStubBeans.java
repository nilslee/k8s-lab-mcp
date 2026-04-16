package com.nilslee.mcp.config;

import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsAgentQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsBuildQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsConsoleQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsJobQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsPluginQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsQueueExecutorQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsSystemQueries;
import org.springframework.http.ResponseEntity;

/**
 * Minimal no-op implementations so {@link com.nilslee.mcp.service.gitops.jenkins.JenkinsCIGitOpsService} can load in
 * tests without Mockito inline (avoids Byte Buddy agent issues on some JDKs / sandboxes).
 */
final class JenkinsQueryStubBeans {

  private static final String EMPTY_JSON = "{}";

  private JenkinsQueryStubBeans() {}

  static JenkinsAgentQueries agentQueries() {
    return new JenkinsAgentQueries() {
      @Override
      public String getAgentComputers() {
        return EMPTY_JSON;
      }

      @Override
      public String getAgentExecutors(String computerName) {
        return EMPTY_JSON;
      }
    };
  }

  static JenkinsBuildQueries buildQueries() {
    return new JenkinsBuildQueries() {
      @Override
      public String getBuildMetadata(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildMetadata(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildTrigger(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildTrigger(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildParameters(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildParameters(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getCommitChangeSets(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getCommitChangeSets(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getArtifacts(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getArtifacts(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getTestReport(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getTestReport(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }

      @Override
      public String getPipelineStatus(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getPipelineStatus(String jobName, Integer buildNumber) {
        return EMPTY_JSON;
      }
    };
  }

  static JenkinsConsoleQueries consoleQueries() {
    return new JenkinsConsoleQueries() {
      @Override
      public ResponseEntity<String> getProgressiveConsoleText(String jobName, Integer offset) {
        return ResponseEntity.ok("");
      }

      @Override
      public ResponseEntity<String> getProgressiveConsoleText(
          String jobName, Integer buildNumber, Integer offset) {
        return ResponseEntity.ok("");
      }
    };
  }

  static JenkinsJobQueries jobQueries() {
    return new JenkinsJobQueries() {
      @Override
      public String getAllJobs() {
        return EMPTY_JSON;
      }

      @Override
      public String getJobHealthReportAndLastBuildRef(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getBuildableAndScm(String jobName) {
        return EMPTY_JSON;
      }

      @Override
      public String getJobProperty(String jobName) {
        return EMPTY_JSON;
      }
    };
  }

  static JenkinsPluginQueries pluginQueries() {
    return new JenkinsPluginQueries() {
      @Override
      public String getPlugins() {
        return EMPTY_JSON;
      }

      @Override
      public String getAvailableUpdates() {
        return EMPTY_JSON;
      }
    };
  }

  static JenkinsQueueExecutorQueries queueExecutorQueries() {
    return new JenkinsQueueExecutorQueries() {
      @Override
      public String getQueueStatus() {
        return EMPTY_JSON;
      }

      @Override
      public String getQueueLabelAndAssigned() {
        return EMPTY_JSON;
      }

      @Override
      public String getComputerStatus() {
        return EMPTY_JSON;
      }
    };
  }

  static JenkinsSystemQueries systemQueries() {
    return new JenkinsSystemQueries() {
      @Override
      public String getOverallReadiness() {
        return EMPTY_JSON;
      }

      @Override
      public String getJenkinsSystemInfo() {
        return EMPTY_JSON;
      }
    };
  }
}
