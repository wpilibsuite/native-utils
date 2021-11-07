package edu.wpi.first.nativeutils.dependencies;

import org.gradle.api.Named;
import org.gradle.nativeplatform.NativeBinarySpec;

public interface NativeDependency extends Named {
    ResolvedNativeDependency resolveNativeDependency(NativeBinarySpec binary, FastDownloadDependencySet loaderDependencySet);
}
