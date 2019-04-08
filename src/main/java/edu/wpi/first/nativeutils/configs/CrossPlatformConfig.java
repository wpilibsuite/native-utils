package edu.wpi.first.nativeutils.configs;

import edu.wpi.first.toolchain.GccToolChain;

public interface CrossPlatformConfig extends PlatformConfig {
  void setToolChain(Class<GccToolChain> toolchain);
  Class<GccToolChain> getToolChain();
}
