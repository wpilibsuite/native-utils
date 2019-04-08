package edu.wpi.first.nativeutils.configs;

import java.util.List;

import org.gradle.nativeplatform.Tool;

public interface CompilerArgsConfig {
  void setArgs(List<String> args);
  List<String> getArgs();

  void setDebugArgs(List<String> args);
  List<String> getDebugArgs();

  void setReleaseArgs(List<String> args);
  List<String> getReleaseArgs();

  default void apply(Tool tool, boolean isDebug) {
    tool.getArgs().addAll(getArgs());
    if (isDebug) {
      tool.getArgs().addAll(getDebugArgs());
    } else {
      tool.getArgs().addAll(getReleaseArgs());
    }
  }
}
