package edu.wpi.first.toolchain.roborio;

import javax.inject.Inject;

import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainExtension;

public abstract class RoboRioToolchainExtension extends OpenSdkToolchainExtension {

    @Inject
    public RoboRioToolchainExtension() {
        super();
        getVersionLow().convention("12.1.0");
        getVersionHigh().convention("12.1.0");
        getToolchainVersion().convention("2023-12.1.0");
    }
}
