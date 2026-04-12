package com.nilslee.mcp.service.gitops.auth;

import com.nilslee.mcp.model.gitops.ArgoCdSessionRequest;
import com.nilslee.mcp.model.gitops.ArgoCdSessionResponse;
import com.nilslee.mcp.model.gitops.ArgoCdUpdatePasswordRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/** Argo CD auth endpoints on {@code /api/v1} (session and account password). Client group: {@code argocd-auth}. */
@HttpExchange("/api/v1")
public interface ArgoCDAuthQueries {

  /**
   * Requires an admin {@code Authorization: Bearer} session token. Argo CD verifies {@code currentPassword}
   * against the authenticated user (admin), not the account named in the body.
   */
  @PutExchange("/account/password")
  void updatePassword(
      @RequestHeader("Authorization") String authorization, @RequestBody ArgoCdUpdatePasswordRequest body);

  @PostExchange("/session")
  ArgoCdSessionResponse createSessionToken(@RequestBody ArgoCdSessionRequest body);
}