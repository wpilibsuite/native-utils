package org.wpilib.nativeutils.dependencies;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

import org.wpilib.nativeutils.NativeUtils;

public abstract class WPIMavenDependency implements NativeDependency {
    private final String name;
    private final Project project;

    private final Map<NativePlatform, Map<BuildType, Optional<ResolvedNativeDependency>>> resolvedDependencies = new HashMap<>();

    private Map<BuildType, Optional<ResolvedNativeDependency>> getInnerMap(NativePlatform platform) {
        Map<BuildType, Optional<ResolvedNativeDependency>> buildTypeMap = resolvedDependencies.get(platform);

        if (buildTypeMap == null) {
            buildTypeMap = new HashMap<>();
            resolvedDependencies.put(platform, buildTypeMap);
        }

        return buildTypeMap;
    }

    protected void addToCache(NativePlatform platform, BuildType buildType, Optional<ResolvedNativeDependency> dependency) {
        Map<BuildType, Optional<ResolvedNativeDependency>> innerMap = getInnerMap(platform);
        innerMap.put(buildType, dependency);
    }

    protected Optional<ResolvedNativeDependency> tryFromCache(NativePlatform platform, BuildType buildType) {
        Map<BuildType, Optional<ResolvedNativeDependency>> innerMap = getInnerMap(platform);
        return innerMap.getOrDefault(buildType, Optional.empty());
    }

    @Inject
    public WPIMavenDependency(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    private static class ViewConfigurationContainer {
        final ArtifactView view;
        final Configuration configuration;

        ViewConfigurationContainer(ArtifactView view, Configuration configuration) {
            this.view = view;
            this.configuration = configuration;
        }
    }

    private final Map<String, ViewConfigurationContainer> classifierViewMap = new HashMap<>();

    protected FileCollection getArtifactRoots(String classifier, ArtifactType type, Optional<FastDownloadDependencySet> loaderDependencySet) {
        if (classifier == null) {
            return project.files();
        }
        ArtifactView view = getViewForArtifact(classifier, type, loaderDependencySet);
        Callable<FileCollection> cbl = () -> view.getFiles();
        return project.files(cbl);
    }

    protected FileCollection getArtifactFiles(String targetPlatform, String buildType, List<String> matches,
            List<String> excludes, ArtifactType type, Optional<FastDownloadDependencySet> loaderDependencySet) {
        buildType = buildType.equalsIgnoreCase("debug") ? "debug" : "";
        ArtifactView view = getViewForArtifact(targetPlatform + buildType, type, loaderDependencySet);
        PatternFilterable filterable = new PatternSet();
        filterable.include(matches);
        filterable.exclude(excludes);
        Callable<Set<File>> cbl = () -> view.getFiles().getAsFileTree().matching(filterable).getFiles();
        return project.files(cbl);
    }

    protected ArtifactView getViewForArtifact(String classifier, ArtifactType type, Optional<FastDownloadDependencySet> loaderDependencySet) {
        ViewConfigurationContainer viewContainer = classifierViewMap.get(classifier);
        String configName = name + "_" + classifier;
        if (viewContainer != null) {
            if (loaderDependencySet.isPresent()) {
                loaderDependencySet.get().addConfiguration(type, viewContainer.configuration);
            }
            return viewContainer.view;
        }

        Configuration cfg = project.getConfigurations().create(configName);
        if (loaderDependencySet.isPresent()) {
            loaderDependencySet.get().addConfiguration(type, cfg);
        }
        String dep = getGroupId().get() + ":" + getArtifactId().get() + ":" + getVersion().get() + ":" + classifier
                + "@" + getExt().get();
        project.getDependencies().add(configName, dep);

        cfg.setCanBeConsumed(false);
        ArtifactView view = cfg.getIncoming().artifactView(viewConfiguration -> {
            viewConfiguration.attributes(attributeContainer -> {
                attributeContainer.attribute(NativeUtils.NATIVE_ARTIFACT_FORMAT,
                        NativeUtils.NATIVE_ARTIFACT_DIRECTORY_TYPE);
            });
        });

        classifierViewMap.put(classifier, new ViewConfigurationContainer(view, cfg));
        return view;
    }

    @Override
    public String getName() {
        return name;
    }

    protected Project getProject() {
        return project;
    }

    public abstract Property<String> getVersion();

    public abstract Property<String> getGroupId();

    public abstract Property<String> getArtifactId();

    public abstract Property<String> getExt();

    public abstract Property<String> getHeaderClassifier();

    public abstract Property<String> getSourceClassifier();

    public abstract SetProperty<String> getTargetPlatforms();

    public abstract SetProperty<String> getExtraSharedExcludes();
}
