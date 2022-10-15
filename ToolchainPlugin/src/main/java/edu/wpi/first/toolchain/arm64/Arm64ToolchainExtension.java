package edu.wpi.first.toolchain.arm64;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class Arm64ToolchainExtension extends OpenSdkToolchainExtension {
    public static final String TOOLCHAIN_VERSION = "2023-10.2.0";
    public static final String INSTALL_SUBDIR = "arm64";

    @Inject
    public Arm64ToolchainExtension() {
        super();
        getVersionLow().convention("10.2.0");
        getVersionHigh().convention("10.2.0");
        getToolchainVersion().convention(TOOLCHAIN_VERSION);
    }
}
