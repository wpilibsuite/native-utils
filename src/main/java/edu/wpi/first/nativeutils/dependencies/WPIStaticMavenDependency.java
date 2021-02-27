package edu.wpi.first.nativeutils.dependencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeBinarySpec;

public abstract class WPIStaticMavenDependency extends WPIMavenDependency {
    public static final List<String> STATIC_MATCHERS = List.of("**/*.lib", "**/*.a");
    public static final List<String> EMPTY_LIST = List.of();

    @Inject
    public WPIStaticMavenDependency(String name, Project project) {
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

        FileCollection linkFiles = getArtifactFiles(platformName + "static", buildType, STATIC_MATCHERS, EMPTY_LIST);
        FileCollection runtimeFiles = getProject().files();

        resolvedDep = new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles);

        resolvedDependencies.put(binary, resolvedDep);
        return resolvedDep;
    }
}
