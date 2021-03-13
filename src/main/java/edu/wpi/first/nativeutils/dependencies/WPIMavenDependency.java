package edu.wpi.first.nativeutils.dependencies;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import edu.wpi.first.nativeutils.NativeUtils;

public abstract class WPIMavenDependency implements NativeDependency {
    private final String name;
    private final Project project;

    @Inject
    public WPIMavenDependency(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    private final Map<String, ArtifactView> classifierViewMap = new HashMap<>();

    protected FileCollection getArtifactRoots(String classifier) {
        if (classifier == null) {
            return project.files();
        }
        ArtifactView view = getViewForArtifact(classifier);
        Callable<FileCollection> cbl = () -> view.getFiles();
        return project.files(cbl);
    }

    protected FileCollection getArtifactFiles(String targetPlatform, String buildType, List<String> matches,
            List<String> excludes) {
        buildType = buildType.equalsIgnoreCase("debug") ? "debug" : "";
        ArtifactView view = getViewForArtifact(targetPlatform + buildType);
        PatternFilterable filterable = new PatternSet();
        filterable.include(matches);
        filterable.exclude(excludes);
        Callable<Set<File>> cbl = () -> view.getFiles().getAsFileTree().matching(filterable).getFiles();
        return project.files(cbl);
    }

    protected ArtifactView getViewForArtifact(String classifier) {
        ArtifactView view = classifierViewMap.get(classifier);
        if (view != null) {
            return view;
        }

        String configName = name + "_" + classifier;
        Configuration cfg = project.getConfigurations().create(configName);
        String dep = getGroupId().get() + ":" + getArtifactId().get() + ":" + getVersion().get() + ":" + classifier
                + "@" + getExt().get();
        project.getDependencies().add(configName, dep);

        cfg.setCanBeConsumed(false);
        view = cfg.getIncoming().artifactView(viewConfiguration -> {
            viewConfiguration.attributes(attributeContainer -> {
                attributeContainer.attribute(NativeUtils.NATIVE_ARTIFACT_FORMAT,
                        NativeUtils.NATIVE_ARTIFACT_DIRECTORY_TYPE);
            });
        });

        classifierViewMap.put(classifier, view);
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
