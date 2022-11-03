package edu.wpi.first.toolchain;

import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;

public class ToolchainRegistrar implements ToolchainRegistrarBase {

    private final String name;

    public ToolchainRegistrar(String name) {
        this.name = name;
    }

    @Override
    public void register(NativeToolChainRegistryInternal registry) {
        registry.registerDefaultToolChain(name, Gcc.class);
    }

    @Override
    public String getName() {
        return name;
    };

}
