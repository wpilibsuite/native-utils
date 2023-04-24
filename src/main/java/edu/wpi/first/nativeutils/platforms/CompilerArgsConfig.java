package edu.wpi.first.nativeutils.platforms;

import java.util.List;
import org.gradle.nativeplatform.Tool;

public interface CompilerArgsConfig {
  List<String> getArgs();

  List<String> getDebugArgs();

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
