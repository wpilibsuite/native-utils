package edu.wpi.first.toolchain.roborio;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;
import javax.inject.Inject;

public abstract class RoboRioToolchainExtension extends OpenSdkToolchainExtension {
  public static final String TOOLCHAIN_VERSION = "2023-12.1.0";
  public static final String INSTALL_SUBDIR = "roborio";

  @Inject
  public RoboRioToolchainExtension() {
    super();
    getVersionLow().convention("12.1.0");
    getVersionHigh().convention("12.1.0");
    getToolchainVersion().convention(TOOLCHAIN_VERSION);
  }
}
