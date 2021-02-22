package edu.wpi.first.nativeutils.dependencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.NativeBinarySpec;

public abstract class WPISharedMavenDependency extends WPIMavenDependency {
    private static final List<String> sharedMatchers = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
    private static final List<String> runtimeMatchers = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll");
    private static final List<String> sharedExcludes = List.of("**/*.so.debug", "**/*.so.*.debug", "**/*jni*");
    private static final List<String> runtimeExcludes = List.of("**/*.so.debug", "**/*.so.*.debug");

    @Inject
    public WPISharedMavenDependency(String name, Project project) {
        super(name, project);
    }

    private final Map<NativeBinarySpec, ResolvedNativeDependency> resolvedDependencies = new HashMap<>();

    public ResolvedNativeDependency resolveNativeDependency(NativeBinarySpec binary) {
        ResolvedNativeDependency resolvedDep = resolvedDependencies.get(binary);
        if (resolvedDep != null) {
            return resolvedDep;
        }

        Set<String> targetPlatforms = getTargetPlatforms().get();
        String platformName = binary.getTargetPlatform().getName();
        if (!targetPlatforms.contains(platformName)) {
            return null;
        }

        String buildType = binary.getBuildType().getName();

        FileCollection headers = getArtifactRoots(getHeaderClassifier().getOrElse(null));
        FileCollection sources = getArtifactRoots(getSourceClassifier().getOrElse(null));

        FileCollection linkFiles = getArtifactFiles(platformName, buildType, sharedMatchers, sharedExcludes);

        FileCollection runtimeFiles;
        if (getSkipAtRuntime().getOrElse(false)) {
            runtimeFiles = getProject().files();
        } else {
            runtimeFiles = getArtifactFiles(platformName, buildType, runtimeMatchers, runtimeExcludes);
        }

        resolvedDep = new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles);

        resolvedDependencies.put(binary, resolvedDep);
        return resolvedDep;
    }

    public abstract Property<Boolean> getSkipAtRuntime();
}
