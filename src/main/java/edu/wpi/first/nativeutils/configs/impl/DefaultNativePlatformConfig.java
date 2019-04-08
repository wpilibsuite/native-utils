package edu.wpi.first.nativeutils.configs.impl;

import edu.wpi.first.nativeutils.configs.NativePlatformConfig;

public class DefaultNativePlatformConfig extends DefaultPlatformConfig implements NativePlatformConfig {
  private boolean skipTests = false;

  public DefaultNativePlatformConfig(String name) {
    super(name);
  }

  /**
   * @param skipTests the skipTests to set
   */
  public void setSkipTests(boolean skipTests) {
    this.skipTests = skipTests;
  }

  @Override
  public boolean getSkipTests() {
    return this.skipTests;
  }
}
