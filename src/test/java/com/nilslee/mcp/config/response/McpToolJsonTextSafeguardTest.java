package com.nilslee.mcp.config.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.nilslee.mcp.aspect.McpToolJsonTextSafeguard;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class McpToolJsonTextSafeguardTest {

  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  @Test
  void plainTextUnchanged() {
    assertThat(McpToolJsonTextSafeguard.apply("not json", jsonMapper)).isEqualTo("not json");
  }

  @Test
  void dataObjectUnchanged() {
    String in = "{\"status\":\"success\",\"data\":{\"resultType\":\"streams\",\"result\":[]}}";
    assertThat(McpToolJsonTextSafeguard.apply(in, jsonMapper)).isEqualTo(in);
  }

  @Test
  void dataArrayBecomesJsonString() throws Exception {
    String in = "{\"status\":\"success\",\"data\":[\"app\",\"job\"]}";
    String out = McpToolJsonTextSafeguard.apply(in, jsonMapper);
    assertThat(out).contains("\"data\":\"[\\\"app\\\",\\\"job\\\"]\"");
    var node = jsonMapper.readTree(out);
    assertThat(node.get("data").isTextual()).isTrue();
    assertThat(node.get("data").asText()).isEqualTo("[\"app\",\"job\"]");
  }
}
