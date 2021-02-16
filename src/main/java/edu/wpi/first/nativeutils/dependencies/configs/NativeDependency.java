package edu.wpi.first.nativeutils.dependencies.configs;

import org.gradle.api.Named;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

public interface NativeDependency extends Named {
    NativeDependencySet getNativeDependencySet(NativeBinarySpec binary);
    boolean appliesTo(NativeBinarySpec binary);
}
