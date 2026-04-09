package com.nilslee.mcp.service.gitops.auth;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

/** Argo CD auth endpoints on {@code /api/v1} (session and account password). Client group: {@code argocd-auth}. */
@HttpExchange("/api/v1")
public interface ArgoCDAuthQueries {

  @PutExchange("/account/password")
  void updatePassword(@RequestBody ArgoCdUpdatePasswordRequest body);

  @PostExchange("/session")
  ArgoCdSessionResponse createSessionToken(@RequestBody ArgoCdSessionRequest body);
}