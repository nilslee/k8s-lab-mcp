package com.nilslee.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

@Component
public class MCPTools {

  private static final Logger log = LoggerFactory.getLogger(MCPTools.class);

  // tool
  @McpTool(name="latest-video", description="I will return the latest video on MCP")
  public String getLatestVideos() {
    var videos = """
        - **Build AI's Future: Model Context Protocol (MCP) with Spring AI in Minutes**

            https://www.youtube.com/watch?v=MarSC2dFA9g
        """;
    return videos;
  }

  // resources

  // prompts
}
