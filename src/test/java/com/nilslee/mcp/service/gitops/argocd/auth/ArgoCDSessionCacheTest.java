package com.nilslee.mcp.service.gitops.argocd.auth;

import com.nilslee.mcp.model.gitops.argocd.ArgoCdSessionResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArgoCDSessionCacheTest {

  @Test
  void getOrCreateSessionToken_throwsWhenTokenMissing() {
    ArgoCDAuthQueries queries = Mockito.mock(ArgoCDAuthQueries.class);
    when(queries.createSessionToken(any())).thenReturn(new ArgoCdSessionResponse(null));
    ArgoCDSessionCache cache = new ArgoCDSessionCache();
    assertThatThrownBy(() -> cache.getOrCreateSessionToken(queries, "u", "p"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no token");
  }

  @Test
  void getOrCreateSessionToken_cachesToken() {
    ArgoCDAuthQueries queries = Mockito.mock(ArgoCDAuthQueries.class);
    when(queries.createSessionToken(any())).thenReturn(new ArgoCdSessionResponse("t1"));
    ArgoCDSessionCache cache = new ArgoCDSessionCache();
    assertThat(cache.getOrCreateSessionToken(queries, "u", "p")).isEqualTo("t1");
    assertThat(cache.getOrCreateSessionToken(queries, "u", "p")).isEqualTo("t1");
    verify(queries, times(1)).createSessionToken(any());
  }

  @Test
  void invalidateSession_forcesNewLogin() {
    ArgoCDAuthQueries queries = Mockito.mock(ArgoCDAuthQueries.class);
    when(queries.createSessionToken(any()))
        .thenReturn(new ArgoCdSessionResponse("a"))
        .thenReturn(new ArgoCdSessionResponse("b"));
    ArgoCDSessionCache cache = new ArgoCDSessionCache();
    assertThat(cache.getOrCreateSessionToken(queries, "u", "p")).isEqualTo("a");
    cache.invalidateSession();
    assertThat(cache.getOrCreateSessionToken(queries, "u", "p")).isEqualTo("b");
    verify(queries, times(2)).createSessionToken(any());
  }
}
