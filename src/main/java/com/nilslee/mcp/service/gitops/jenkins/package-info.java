/**
 * Read-only Jenkins CI diagnostics aggregated for MCP tools.
 *
 * <p><strong>Limitations (by design or environment):</strong>
 *
 * <ul>
 *   <li>Console {@code progressiveText} responses are capped by {@code mcp.gitops.jenkins.max-log-chars} and
 *       {@code mcp.gitops.jenkins.max-progressive-chunks} to avoid unbounded MCP payloads and stuck {@code X-More-Data}
 *       loops.
 *   <li>Log tail tools stream progressive chunks and keep only the last N complete lines plus one trailing fragment;
 *       very long lines still count toward {@code max-log-chars} raw scan budget.
 *   <li>Some Jenkins APIs return 404 for missing test reports or non-workflow jobs; callers see JSON error bodies
 *       as returned by the server.
 * </ul>
 */
package com.nilslee.mcp.service.gitops.jenkins;
