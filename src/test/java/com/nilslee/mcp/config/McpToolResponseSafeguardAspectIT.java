package com.nilslee.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.nilslee.mcp.aspect.McpToolResponseSafeguardAspect;
import com.nilslee.mcp.tools.LogTools;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ensures tool beans are proxied so {@link McpToolResponseSafeguardAspect} runs when Spring AI
 * invokes {@link org.springframework.ai.mcp.annotation.McpTool} methods on the context bean.
 */
@SpringBootTest
class McpToolResponseSafeguardAspectIT {

  @Autowired LogTools logTools;

  @Test
  void toolBeansAreSpringProxies() {
    assertThat(AopUtils.isAopProxy(logTools)).isTrue();
  }
}
