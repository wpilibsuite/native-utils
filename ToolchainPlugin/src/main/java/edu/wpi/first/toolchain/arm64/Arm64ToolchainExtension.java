package edu.wpi.first.toolchain.arm64;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class Arm64ToolchainExtension extends OpenSdkToolchainExtension {

    @Inject
    public Arm64ToolchainExtension() {
        super();
        versionLow = "10.2.0";
        versionHigh = "10.2.0";
        toolchainVersion = "2023-10.2.0";
    }
}
