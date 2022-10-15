package edu.wpi.first.toolchain.roborio;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class RoboRioToolchainExtension extends OpenSdkToolchainExtension {
    public static final String TOOLCHAIN_VERSION = "2023-12.1.0";
    public static final String INSTALL_SUBDIR = "roborio";

    @Inject
    public RoboRioToolchainExtension() {
        super();
        versionLow = "12.1.0";
        versionHigh = "12.1.0";
        toolchainVersion = TOOLCHAIN_VERSION;
    }
}
