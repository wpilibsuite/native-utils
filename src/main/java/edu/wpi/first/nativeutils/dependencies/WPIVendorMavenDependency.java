package edu.wpi.first.nativeutils.dependencies;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension;

public abstract class WPIVendorMavenDependency extends WPIMavenDependency {

    private WPIVendorDepsExtension.CppArtifact artifact;

    @Inject
    public WPIVendorMavenDependency(String name, Project project) {
        super(name, project);
    }

    @Override
    public Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType,
            Optional<FastDownloadDependencySet> loaderDependencySet) {
        Optional<ResolvedNativeDependency> resolvedDep = tryFromCache(platform, buildType);
        if (resolvedDep.isPresent()) {
            return resolvedDep;
        }

        if (artifact.version.equals("wpilib")) {
            WPIVendorDepsExtension wpi = getProject().getExtensions().getByType(WPIVendorDepsExtension.class);
            getVersion().set(wpi.getFixedVersion());
        } else {
            getVersion().set(artifact.version);
        }

        getGroupId().set(artifact.groupId);
        getArtifactId().set(artifact.artifactId);
        getExt().set("zip");
        getHeaderClassifier().set(artifact.headerClassifier);
        getSourceClassifier().set(artifact.sourcesClassifier);

        getTargetPlatforms().addAll(artifact.binaryPlatforms);

        FileCollection headers = getArtifactRoots(getHeaderClassifier().getOrElse(null), ArtifactType.HEADERS,
                loaderDependencySet);
        FileCollection sources = getArtifactRoots(getSourceClassifier().getOrElse(null), ArtifactType.SOURCES,
                loaderDependencySet);

        Set<String> targetPlatforms = getTargetPlatforms().get();
        String platformName = platform.getName();
        if (!targetPlatforms.contains(platformName)) {
            if (artifact.skipInvalidPlatforms) {
                // return empty
                Optional<ResolvedNativeDependency> dep = Optional
                        .of(new ResolvedNativeDependency(headers, sources, getProject().files(),
                                getProject().files()));
                addToCache(platform, buildType, resolvedDep);
                return dep;
            } else {
                throw new MissingVendorDependencyPlatformException(artifact.artifactId, platformName);
            }
        }

        String buildTypeName = buildType.getName();

        FileCollection linkFiles;
        FileCollection runtimeFiles;

        if (artifact.sharedLibrary) {
            linkFiles = getArtifactFiles(platformName, buildTypeName, WPISharedMavenDependency.SHARED_MATCHERS,
                    WPISharedMavenDependency.SHARED_EXCLUDES, ArtifactType.LINK, loaderDependencySet);
            runtimeFiles = getArtifactFiles(platformName, buildTypeName, WPISharedMavenDependency.RUNTIME_MATCHERS,
                    WPISharedMavenDependency.RUNTIME_EXCLUDES, ArtifactType.RUNTIME, loaderDependencySet);
        } else {
            linkFiles = getArtifactFiles(platformName + "static", buildTypeName,
                    WPIStaticMavenDependency.STATIC_MATCHERS, WPIStaticMavenDependency.EMPTY_LIST, ArtifactType.LINK,
                    loaderDependencySet);
            runtimeFiles = getProject().files();
        }

        resolvedDep = Optional.of(new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles));

        addToCache(platform, buildType, resolvedDep);
        return resolvedDep;
    }

    public void setArtifact(WPIVendorDepsExtension.CppArtifact artifact) {
        this.artifact = artifact;
    }

    public WPIVendorDepsExtension.CppArtifact getArtifact() {
        return artifact;
    }
}
