package edu.wpi.first.nativeutils.dependencies;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.NativeBinarySpec;

public abstract class WPIHeaderOnlyMavenDependency extends WPIMavenDependency {
    @Inject
    public WPIHeaderOnlyMavenDependency(String name, Project project) {
        super(name, project);
    }

    private final Map<NativeBinarySpec, ResolvedNativeDependency> resolvedDependencies = new HashMap<>();

    @Override
    public ResolvedNativeDependency resolveNativeDependency(NativeBinarySpec binary, FastDownloadDependencySet loaderDependencySet) {
        ResolvedNativeDependency resolvedDep = resolvedDependencies.get(binary);
        if (resolvedDep != null) {
            return resolvedDep;
        }

        FileCollection headers = getArtifactRoots(getHeaderClassifier().getOrElse(null), ArtifactType.HEADERS, loaderDependencySet);

        FileCollection sources = getProject().files();
        FileCollection linkFiles = getProject().files();
        FileCollection runtimeFiles = getProject().files();

        resolvedDep = new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles);

        resolvedDependencies.put(binary, resolvedDep);
        return resolvedDep;
    }

    public abstract Property<Boolean> getSkipAtRuntime();
}
