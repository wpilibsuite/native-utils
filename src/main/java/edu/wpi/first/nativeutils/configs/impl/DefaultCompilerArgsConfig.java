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
  public List<String> getArgs() {
    return args;
  }

  /**
   * @param args the args to set
   */
  public void setArgs(List<String> args) {
    this.args = args;
  }

  /**
   * @return the debugArgs
   */
  public List<String> getDebugArgs() {
    return debugArgs;
  }

  /**
   * @param debugArgs the debugArgs to set
   */
  public void setDebugArgs(List<String> debugArgs) {
    this.debugArgs = debugArgs;
  }

  /**
   * @return the releaseArgs
   */
  public List<String> getReleaseArgs() {
    return releaseArgs;
  }

  /**
   * @param releaseArgs the releaseArgs to set
   */
  public void setReleaseArgs(List<String> releaseArgs) {
    this.releaseArgs = releaseArgs;
  }
}
