package org.wpilib.toolchain.arm64;

import javax.inject.Inject;

import org.wpilib.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class Arm64ToolchainExtension extends OpenSdkToolchainExtension {
    public static final String TOOLCHAIN_VERSION = "2027-14.3.0";
    public static final String INSTALL_SUBDIR = "arm64";

    @Inject
    public Arm64ToolchainExtension() {
        super();
        getVersionLow().convention("14.3.0");
        getVersionHigh().convention("14.3.0");
        getToolchainVersion().convention(TOOLCHAIN_VERSION);
    }
}
