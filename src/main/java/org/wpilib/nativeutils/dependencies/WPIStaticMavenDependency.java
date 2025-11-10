package org.wpilib.nativeutils.dependencies;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

public abstract class WPIStaticMavenDependency extends WPIMavenDependency {
    public static final List<String> STATIC_MATCHERS = List.of("**/*.lib", "**/*.a");
    public static final List<String> EMPTY_LIST = List.of();

    @Inject
    public WPIStaticMavenDependency(String name, Project project) {
        super(name, project);
    }

    @Override
    public Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType, Optional<FastDownloadDependencySet> loaderDependencySet) {
        Optional<ResolvedNativeDependency> resolvedDep = tryFromCache(platform, buildType);
        if (resolvedDep.isPresent()) {
            return resolvedDep;
        }

        Set<String> targetPlatforms = getTargetPlatforms().get();
        String platformName = platform.getName();
        if (!targetPlatforms.contains(platformName)) {
            return null;
        }

        String buildTypeName = buildType.getName();

        FileCollection headers = getArtifactRoots(getHeaderClassifier().getOrElse(null), ArtifactType.HEADERS, loaderDependencySet);
        FileCollection sources = getArtifactRoots(getSourceClassifier().getOrElse(null), ArtifactType.SOURCES, loaderDependencySet);

        FileCollection linkFiles = getArtifactFiles(platformName + "static", buildTypeName, STATIC_MATCHERS, EMPTY_LIST, ArtifactType.LINK, loaderDependencySet);
        FileCollection runtimeFiles = getProject().files();

        resolvedDep = Optional.of(new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles));

        addToCache(platform, buildType, resolvedDep);
        return resolvedDep;
    }
}
