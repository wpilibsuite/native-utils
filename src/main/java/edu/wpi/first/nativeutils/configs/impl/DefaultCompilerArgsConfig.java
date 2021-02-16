package edu.wpi.first.nativeutils.configs.impl;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.nativeutils.configs.CompilerArgsConfig;

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
