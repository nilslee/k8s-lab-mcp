package com.nilslee.mcp.config.gitops.jenkins;

import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsAgentQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsBuildQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsConsoleQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsJobQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsPluginQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsQueueExecutorQueries;
import com.nilslee.mcp.service.gitops.jenkins.query.JenkinsSystemQueries;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Registers Jenkins declarative HTTP clients for the {@code jenkins} service-client group.
 *
 * <p>Each {@link org.springframework.web.service.annotation.HttpExchange} interface below is bound to
 * {@code spring.http.serviceclient.jenkins} (base URL and timeouts). Optional
 * {@link RestClientHttpServiceGroupConfigurer} hooks can add CSRF crumbs or basic auth later.
 *
 * @see JenkinsConfigurationProperties
 */
@Configuration
@EnableConfigurationProperties(JenkinsConfigurationProperties.class)
@ImportHttpServices(group = "jenkins", types = JenkinsAgentQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsBuildQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsConsoleQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsJobQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsPluginQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsQueueExecutorQueries.class)
@ImportHttpServices(group = "jenkins", types = JenkinsSystemQueries.class)
public class JenkinsHttpRestClientConfiguration {

  @Bean
  RestClientHttpServiceGroupConfigurer jenkinsHttpServiceGroupConfigurer(JenkinsConfigurationProperties props) {
    return groups ->
        groups.filterByName("jenkins").forEachClient((group, clientBuilder) -> {
          clientBuilder.defaultHeaders(httpHeaders -> {
            httpHeaders.setBasicAuth(props.username(), props.password());
          });
        });
  }
}
