package org.wpilib.toolchain.roborio;

import javax.inject.Inject;

import org.wpilib.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class RoboRioToolchainExtension extends OpenSdkToolchainExtension {
    public static final String TOOLCHAIN_VERSION = "2025-12.1.0";
    public static final String INSTALL_SUBDIR = "roborio";

    @Inject
    public RoboRioToolchainExtension() {
        super();
        getVersionLow().convention("12.1.0");
        getVersionHigh().convention("12.1.0");
        getToolchainVersion().convention(TOOLCHAIN_VERSION);
    }
}
