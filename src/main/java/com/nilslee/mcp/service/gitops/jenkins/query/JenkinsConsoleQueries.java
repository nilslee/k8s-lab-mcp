package com.nilslee.mcp.service.gitops.jenkins.query;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Progressive console output ({@code logText/progressiveText}) for Jenkins builds.
 *
 * <p>Paths are relative to the {@code jenkins} HTTP service client base URL.
 *
 * <p>Jenkins sets response headers on progressive responses:
 *
 * <ul>
 *   <li>{@code X-More-Data}: {@code yes} while more log bytes remain
 *   <li>{@code X-Text-Size}: byte offset to pass as {@code start} on the next request
 * </ul>
 */
@HttpExchange
public interface JenkinsConsoleQueries {

  /**
   * Progressive console text for the job's last build.
   *
   * @param jobName job path segment as in {@code /job/{jobName}/...}
   * @param offset {@code start} query parameter (byte offset from Jenkins)
   */
  @GetExchange("/job/{jobName}/lastBuild/logText/progressiveText?start={offset}")
  ResponseEntity<String> getProgressiveConsoleText(
      @PathVariable String jobName, @PathVariable Integer offset);

  /**
   * Progressive console text for a specific build number.
   *
   * @param jobName job path segment
   * @param buildNumber build number
   * @param offset {@code start} query parameter
   */
  @GetExchange("/job/{jobName}/{buildNumber}/logText/progressiveText?start={offset}")
  ResponseEntity<String> getProgressiveConsoleText(
      @PathVariable String jobName,
      @PathVariable Integer buildNumber,
      @PathVariable Integer offset);
}
