package org.wpilib.toolchain.systemcore;

import javax.inject.Inject;

import org.wpilib.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class SystemCoreToolchainExtension extends OpenSdkToolchainExtension {
    public static final String TOOLCHAIN_VERSION = "2025-12.2.0";
    public static final String INSTALL_SUBDIR = "systemcore";

    @Inject
    public SystemCoreToolchainExtension() {
        super();
        getVersionLow().convention("12.2.0");
        getVersionHigh().convention("12.2.0");
        getToolchainVersion().convention(TOOLCHAIN_VERSION);
    }
}
