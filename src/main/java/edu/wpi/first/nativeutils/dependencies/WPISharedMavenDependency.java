package edu.wpi.first.nativeutils.dependencies;

import java.util.ArrayList;
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
    public static final List<String> SHARED_MATCHERS = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
    public static final List<String> RUNTIME_MATCHERS = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll", "**/*.pdb");
    public static final List<String> SHARED_EXCLUDES = List.of("**/*.so.debug", "**/*.so.*.debug", "**/*jni*");
    public static final List<String> RUNTIME_EXCLUDES = List.of();

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

        List<String> sharedExcludes = SHARED_EXCLUDES;
        Set<String> extraExcludes = getExtraSharedExcludes().get();
        if (!extraExcludes.isEmpty()) {
            sharedExcludes = new ArrayList<>(sharedExcludes);
            sharedExcludes.addAll(extraExcludes);
        }

        FileCollection linkFiles = getArtifactFiles(platformName, buildType, SHARED_MATCHERS, sharedExcludes);

        FileCollection runtimeFiles;
        if (getSkipAtRuntime().getOrElse(false)) {
            runtimeFiles = getProject().files();
        } else {
            runtimeFiles = getArtifactFiles(platformName, buildType, RUNTIME_MATCHERS, RUNTIME_EXCLUDES);
        }

        resolvedDep = new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles);

        resolvedDependencies.put(binary, resolvedDep);
        return resolvedDep;
    }

    public abstract Property<Boolean> getSkipAtRuntime();
}
