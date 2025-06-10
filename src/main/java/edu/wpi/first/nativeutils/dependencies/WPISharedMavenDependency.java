package edu.wpi.first.nativeutils.dependencies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.provider.ListProperty;

public abstract class WPISharedMavenDependency extends WPIMavenDependency {
    public static final List<String> SHARED_MATCHERS = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
    public static final List<String> RUNTIME_MATCHERS = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll", "**/*.pdb");
    public static final List<String> SHARED_EXCLUDES = List.of("**/*.so.debug", "**/*.so.*.debug", "**/*jni*");
    public static final List<String> RUNTIME_EXCLUDES = List.of();
    protected final NamedDomainObjectCollection<NativeDependency> dependencyCollection;

    @Inject
    public WPISharedMavenDependency(String name, Project project, NamedDomainObjectCollection<NativeDependency> dependencyCollection) {
        super(name, project);
        this.dependencyCollection = dependencyCollection;
    }
    
    public abstract ListProperty<String> getDependencies();

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

        List<String> sharedExcludes = SHARED_EXCLUDES;
        Set<String> extraExcludes = getExtraSharedExcludes().get();
        if (!extraExcludes.isEmpty()) {
            sharedExcludes = new ArrayList<>(sharedExcludes);
            sharedExcludes.addAll(extraExcludes);
        }

        FileCollection linkFiles = getArtifactFiles(platformName, buildTypeName, SHARED_MATCHERS, sharedExcludes, ArtifactType.LINK, loaderDependencySet);

        FileCollection runtimeFiles;
        if (getSkipAtRuntime().getOrElse(false)) {
            runtimeFiles = getProject().files();
        } else {
            runtimeFiles = getArtifactFiles(platformName, buildTypeName, RUNTIME_MATCHERS, RUNTIME_EXCLUDES, ArtifactType.RUNTIME, loaderDependencySet);
        }

        
        List<String> dependencies = getDependencies().get();
        for (String dep : dependencies) {
            ResolvedNativeDependency resolved = dependencyCollection.getByName(dep).resolveNativeDependency(platform, buildType, loaderDependencySet).get();
            headers = headers.plus(resolved.getIncludeRoots());
            sources = sources.plus(resolved.getSourceRoots());
            linkFiles = linkFiles.plus(resolved.getLinkFiles());
            runtimeFiles = runtimeFiles.plus(resolved.getRuntimeFiles());
        }

        resolvedDep = Optional.of(new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles));

        addToCache(platform, buildType, resolvedDep);
        return resolvedDep;
    }

    public abstract Property<Boolean> getSkipAtRuntime();
}
