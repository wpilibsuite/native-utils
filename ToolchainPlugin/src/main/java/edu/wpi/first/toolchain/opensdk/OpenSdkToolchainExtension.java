package edu.wpi.first.toolchain.opensdk;

import javax.inject.Inject;

import org.gradle.api.provider.Property;

public abstract class OpenSdkToolchainExtension {
    public abstract Property<String> getVersionLow();
    public abstract Property<String> getVersionHigh();
    public abstract Property<String> getToolchainVersion();
    public abstract Property<String> getToolchainTag();

    @Inject
    public OpenSdkToolchainExtension() {
        getToolchainTag().convention("v2023-3");
    }
}
