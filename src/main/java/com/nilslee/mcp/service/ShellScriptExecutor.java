package com.nilslee.mcp.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
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
      // In the Docker image, WORKDIR is /app and scripts live under /app/scripts. Some JVMs or
      // launchers leave user.dir elsewhere; relative paths like ./scripts/cluster-resources/foo.sh
      // must resolve from /app.
      File appRoot = new File("/app");
      if (new File(appRoot, "scripts").isDirectory()) {
        pb.directory(appRoot);
      }
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