package org.wpilib.nativeutils.dependencies;

import java.util.Optional;

import org.gradle.api.Named;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

public interface NativeDependency extends Named {
    Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType, Optional<FastDownloadDependencySet> loaderDependencySet);
}
