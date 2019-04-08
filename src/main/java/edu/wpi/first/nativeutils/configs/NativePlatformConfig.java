package edu.wpi.first.nativeutils.configs;

public interface NativePlatformConfig extends PlatformConfig {
  void setSkipTests(boolean skip);
  boolean getSkipTests();
}
