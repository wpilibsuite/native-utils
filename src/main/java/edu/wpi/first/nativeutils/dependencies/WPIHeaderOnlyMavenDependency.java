package edu.wpi.first.nativeutils.dependencies;

import java.util.Optional;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

public abstract class WPIHeaderOnlyMavenDependency extends WPIMavenDependency {
    @Inject
    public WPIHeaderOnlyMavenDependency(String name, Project project) {
        super(name, project);
    }

    @Override
    public Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType, Optional<FastDownloadDependencySet> loaderDependencySet) {
        Optional<ResolvedNativeDependency> resolvedDep = tryFromCache(platform, buildType);
        if (resolvedDep.isPresent()) {
            return resolvedDep;
        }

        FileCollection headers = getArtifactRoots(getHeaderClassifier().getOrElse(null), ArtifactType.HEADERS, loaderDependencySet);

        FileCollection sources = getProject().files();
        FileCollection linkFiles = getProject().files();
        FileCollection runtimeFiles = getProject().files();

        resolvedDep = Optional.of(new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles));

        addToCache(platform, buildType, resolvedDep);
        return resolvedDep;
    }

    public abstract Property<Boolean> getSkipAtRuntime();
}
