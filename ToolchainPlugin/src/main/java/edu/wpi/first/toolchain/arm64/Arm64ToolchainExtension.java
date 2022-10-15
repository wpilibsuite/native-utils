package edu.wpi.first.toolchain.arm64;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class Arm64ToolchainExtension extends OpenSdkToolchainExtension {

    @Inject
    public Arm64ToolchainExtension() {
        super();
        getVersionLow().convention("10.2.0");
        getVersionHigh().convention("10.2.0");
        getToolchainVersion().convention("2023-10.2.0");
    }
}
