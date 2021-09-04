package edu.wpi.first.nativeutils.platforms;

import java.util.ArrayList;
import java.util.List;

public class DefaultCompilerArgsConfig implements CompilerArgsConfig {
  private List<String> args = new ArrayList<>();
  private List<String> debugArgs = new ArrayList<>();
  private List<String> releaseArgs = new ArrayList<>();

  /**
   * @return the args
   */
  @Override
  public List<String> getArgs() {
    return args;
  }

  /**
   * @return the debugArgs
   */
  @Override
  public List<String> getDebugArgs() {
    return debugArgs;
  }

  /**
   * @return the releaseArgs
   */
  @Override
  public List<String> getReleaseArgs() {
    return releaseArgs;
  }
}
