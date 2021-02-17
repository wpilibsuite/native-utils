package edu.wpi.first.nativeutils.dependencies.configs;

import org.gradle.api.Named;
import org.gradle.nativeplatform.NativeBinarySpec;

import edu.wpi.first.nativeutils.dependencies.ResolvedNativeDependency;

public interface NativeDependency extends Named {
    ResolvedNativeDependency resolveNativeDependency(NativeBinarySpec binary);
    boolean appliesTo(NativeBinarySpec binary);
}
