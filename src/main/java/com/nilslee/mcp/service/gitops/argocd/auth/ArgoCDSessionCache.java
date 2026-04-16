package com.nilslee.mcp.service.gitops.argocd.auth;

import com.nilslee.mcp.model.gitops.argocd.ArgoCdSessionRequest;
import com.nilslee.mcp.model.gitops.argocd.ArgoCdSessionResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the Argo CD JWT from {@code POST /api/v1/session} for the {@code argocd-data} client. Cleared on
 * {@link #invalidateSession()} so the next request obtains a fresh token (handles restarts and expiry).
 */
@Component
public class ArgoCDSessionCache {

  private final AtomicReference<String> sessionToken = new AtomicReference<>();

  public void invalidateSession() {
    sessionToken.set(null);
  }

  /**
   * Returns the cached token, or logs in and caches a new one.
   *
   * @throws IllegalStateException if the session response has no usable token (e.g. JSON mismatch or empty body)
   */
  public String getOrCreateSessionToken(
      ArgoCDAuthQueries queries, String username, String password) {
    return sessionToken.updateAndGet(
        current -> {
          if (current != null) {
            return current;
          }
          ArgoCdSessionResponse session =
              queries.createSessionToken(new ArgoCdSessionRequest(username, password));
          String t = session.token();
          if (t == null || t.isBlank()) {
            throw new IllegalStateException(
                "Argo CD POST /api/v1/session returned no token. Check mcp.gitops.argocd: "
                    + "service-account-name password in Kubernetes secret "
                    + "(and argocd-initial-admin-secret if bootstrapping), "
                    + "that the local user has login capability, and argocd-auth base-url.");
          }
          return t;
        });
  }
}
