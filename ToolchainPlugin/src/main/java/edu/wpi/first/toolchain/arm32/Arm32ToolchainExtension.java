package edu.wpi.first.toolchain.arm32;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class Arm32ToolchainExtension extends OpenSdkToolchainExtension {

    @Inject
    public Arm32ToolchainExtension() {
        super();
        versionLow = "10.2.0";
        versionHigh = "10.2.0";
        toolchainVersion = "2023-10.2.0";
    }
}
