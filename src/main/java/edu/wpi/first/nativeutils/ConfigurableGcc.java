package edu.wpi.first.nativeutils;

import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.ToolchainOptions;

public class ConfigurableGcc extends GccToolChain {
  public ConfigurableGcc(ToolchainOptions options) {
    super(options);
  }

  @Override
  protected String getTypeName() {
    return this.getName() + "ConfiguredGcc";
  }
}
