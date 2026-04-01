package com.nilslee.mcp.tools;

import com.nilslee.mcp.service.ShellScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemTools {

  private static final Logger log = LoggerFactory.getLogger(SystemTools.class);
  private final ShellScriptExecutor executor;

  @Autowired
  public SystemTools(ShellScriptExecutor executor) {
    this.executor = executor;
  }

  @McpTool(name="echo-hello-world", description="I will run the test echo file for printing hello world")
  public String echoHelloWorld() {
    return executor.execute("./scripts/echo.sh", List.of());
  }
}
