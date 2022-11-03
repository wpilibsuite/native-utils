package edu.wpi.first.toolchain;

import org.gradle.api.Named;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;

interface ToolchainRegistrarBase extends Named {
 void register(NativeToolChainRegistryInternal registry);
}
