package edu.wpi.first.nativeutils.configs.impl;

import edu.wpi.first.nativeutils.configs.CrossPlatformConfig;
import edu.wpi.first.toolchain.GccToolChain;

public class DefaultCrossPlatformConfig extends DefaultPlatformConfig implements CrossPlatformConfig {
  public DefaultCrossPlatformConfig(String name) {
    super(name);
  }

  private Class<GccToolChain> toolChain = null;

  /**
   * @return the toolChain
   */
  public Class<GccToolChain> getToolChain() {
    return toolChain;
  }

  /**
   * @param toolChain the toolChain to set
   */
  public void setToolChain(Class<GccToolChain> toolChain) {
    this.toolChain = toolChain;
  }
}
