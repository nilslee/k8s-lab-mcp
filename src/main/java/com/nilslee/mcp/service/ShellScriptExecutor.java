package com.nilslee.mcp.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShellScriptExecutor {

  public String execute(String scriptPath, List<String> args) {
    try {
      List<String> command = new ArrayList<>();
      command.add("bash");
      command.add(scriptPath);
      command.addAll(args);

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.redirectErrorStream(true);

      Process process = pb.start();
      StringBuilder output = new StringBuilder();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        return "Error: Script exited with code " + exitCode + "\n" + output;
      }
      return output.toString();
    } catch (Exception e) {
      return "Execution failed: " + e.getMessage();
    }
  }
}