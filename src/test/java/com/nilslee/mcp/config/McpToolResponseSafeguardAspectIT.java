package com.nilslee.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.nilslee.mcp.aspect.json_safeguard.McpToolResponseSafeguardAspect;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsAgentQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsBuildQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsConsoleQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsJobQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsPluginQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsQueueExecutorQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsSystemQueries;
import com.nilslee.mcp.tools.gitops.ArgoCDTools;
import com.nilslee.mcp.tools.gitops.JenkinsTools;
import com.nilslee.mcp.tools.LogTools;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Ensures tool beans are proxied so {@link McpToolResponseSafeguardAspect} runs when Spring AI
 * invokes {@link org.springframework.ai.mcp.annotation.McpTool} methods on the context bean.
 */
@SpringBootTest(properties = "mcp.gitops.jenkins.enabled=true")
@Import(McpToolResponseSafeguardAspectIT.JenkinsQueryStubConfiguration.class)
class McpToolResponseSafeguardAspectIT {

  @Autowired LogTools logTools;

  @Autowired
  ArgoCDTools argoCDTools;

  @Autowired JenkinsTools jenkinsTools;

  @TestConfiguration
  static class JenkinsQueryStubConfiguration {

    @Bean
    @Primary
    JenkinsAgentQueries jenkinsAgentQueries() {
      return JenkinsQueryStubBeans.agentQueries();
    }

    @Bean
    @Primary
    JenkinsBuildQueries jenkinsBuildQueries() {
      return JenkinsQueryStubBeans.buildQueries();
    }

    @Bean
    @Primary
    JenkinsConsoleQueries jenkinsConsoleQueries() {
      return JenkinsQueryStubBeans.consoleQueries();
    }

    @Bean
    @Primary
    JenkinsJobQueries jenkinsJobQueries() {
      return JenkinsQueryStubBeans.jobQueries();
    }

    @Bean
    @Primary
    JenkinsPluginQueries jenkinsPluginQueries() {
      return JenkinsQueryStubBeans.pluginQueries();
    }

    @Bean
    @Primary
    JenkinsQueueExecutorQueries jenkinsQueueExecutorQueries() {
      return JenkinsQueryStubBeans.queueExecutorQueries();
    }

    @Bean
    @Primary
    JenkinsSystemQueries jenkinsSystemQueries() {
      return JenkinsQueryStubBeans.systemQueries();
    }
  }

  @Test
  void toolBeansAreSpringProxies() {
    assertThat(AopUtils.isAopProxy(logTools)).isTrue();
    assertThat(AopUtils.isAopProxy(argoCDTools)).isTrue();
    assertThat(AopUtils.isAopProxy(jenkinsTools)).isTrue();
  }
}
