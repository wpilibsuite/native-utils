package edu.wpi.first.nativeutils.dependencies.configs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ArtifactView.ViewConfiguration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.nativeutils.dependencies.ResolvedNativeDependency;

public abstract class WPIMavenDependency implements NativeDependency, HasConfigurableValue {
    private static final List<String> sharedMatchers = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
    private static final List<String> runtimeMatchers = List.of("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll");
    private static final List<String> sharedExcludes = List.of("**/*.so.debug", "**/*.so.*.debug");
    private static final List<String> runtimeExcludes = List.of("**/*.so.debug", "**/*.so.*.debug");
    private static final List<String> staticMatchers = List.of("**/*.lib", "**/*.a");
    private static final List<String> emptyList = List.of();

    private boolean finalizeOnRead = false;
    private boolean hasBeenFinalized = false;
    private boolean disallowUnsafeRead = false;
    private final String name;
    private final Project project;

    //private final FileCollection includeRoots;
    //private final FileCollection linkFiles;
    //private final ProviderFactory providerFactory;


    @Inject
    public WPIMavenDependency(String name, Project project, ProviderFactory providerFactory) {
        this.name = name;
        this.project = project;
        //this.providerFactory = providerFactory;

        //includeRoots = getFilesForArtifact(getHeaderClassifier(), true);
        //includeRoots = getFilesForArtifact(getHeaderClassifier(), true);
    }

    private FileCollection getFilesForArtifact(String classifier, boolean rooted, List<String> matches, List<String> excludes) {
        String configName = name + "_" + classifier;
        Configuration cfg = project.getConfigurations().create(configName);
        String dep = getGroupId().get() + ":" + getArtifactId().get() + ":" + getVersion().get() + ":" + classifier
                + "@" + getExt().get();
        project.getDependencies().add(configName, dep);
        ArtifactView includeDirs = cfg.getIncoming().artifactView(new Action<ViewConfiguration>() {
            @Override
            public void execute(ViewConfiguration viewConfiguration) {
                viewConfiguration.attributes(new Action<AttributeContainer>() {
                    @Override
                    public void execute(AttributeContainer attributeContainer) {
                        attributeContainer.attribute(ArtifactAttributes.ARTIFACT_FORMAT,
                                ArtifactTypeDefinition.DIRECTORY_TYPE);
                    }
                });
            }
        });
        if (rooted) {
            Callable<FileCollection> cbl = () -> includeDirs.getFiles();
            return project.files(cbl);
        } else {
            PatternFilterable filterable = new PatternSet();
            filterable.include(matches);
            filterable.exclude(excludes);
            Callable<Set<File>> cbl = () -> includeDirs.getFiles().getAsFileTree().matching(filterable).getFiles();
            return project.files(cbl);
        }
    }

    private final Map<String, FileCollection> resolvedCollections = new HashMap<>();
    private final Map<NativeBinarySpec, ResolvedNativeDependency> resolvedDependencies = new HashMap<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ResolvedNativeDependency resolveNativeDependency(NativeBinarySpec binary) {
        // Check if we've been finalized
        if (!hasBeenFinalized && disallowUnsafeRead) {
            // TODO proper exceptions
            throw new GradleException("Value not finalized, unsafe read");
        }
        if (finalizeOnRead) {
            hasBeenFinalized = true;
        }

        ResolvedNativeDependency resolvedDep = resolvedDependencies.get(binary);
        if (resolvedDep != null) {
            return resolvedDep;
        }

        getSharedPlatforms().finalizeValue();
        getStaticPlatforms().finalizeValue();
        getHeaderClassifier().finalizeValue();
        getSourceClassifier().finalizeValue();
        getVersion().finalizeValue();
        // TODO finish these

        Set<String> sharedPlatforms = getSharedPlatforms().get();
        Set<String> staticPlatforms = getStaticPlatforms().get();

        String platformName = binary.getTargetPlatform().getName();
        List<String> linkMatchers;
        List<String> linkExcludes;
        List<String> runtimeMatchers;
        List<String> runtimeExcludes;

        if (sharedPlatforms.contains(platformName)) {
            linkMatchers = sharedMatchers;
            linkExcludes = sharedExcludes;
            runtimeMatchers = WPIMavenDependency.runtimeMatchers;
            runtimeExcludes = WPIMavenDependency.runtimeExcludes;
        } else if (staticPlatforms.contains(platformName)) {
            linkMatchers = staticMatchers;
            linkExcludes = emptyList;
            runtimeMatchers = emptyList;
            runtimeExcludes = emptyList;
        } else {
            return null;
        }

        String headerClassifier = getHeaderClassifier().getOrElse(null);
        String sourceClassifier = getSourceClassifier().getOrElse(null);

        resolvedDep =
            new ResolvedNativeDependency(
                resolveFileArtifact(headerClassifier, true, null, null),
                resolveFileArtifact(sourceClassifier, true, null, null),
                resolveFileArtifact(platformName, false, linkMatchers, linkExcludes),
                resolveFileArtifact(platformName, false, runtimeMatchers, runtimeExcludes));

        resolvedDependencies.put(binary, resolvedDep);
        return resolvedDep;
        // return new NativeDependencySet() {

        //     @Override
        //     public FileCollection getIncludeRoots() {
        //         Callable<File> cbl = () -> headers.call().getDir();
        //         return project.files(cbl);
        //     }

        //     @Override
        //     public FileCollection getLinkFiles() {
        //         return project.files();
        //     }

        //     @Override
        //     public FileCollection getRuntimeFiles() {
        //         return project.files();
        //     }

        // };
    }

    private FileCollection resolveFileArtifact(String classifier, boolean rooted, List<String> matches, List<String> excludes) {
        if (!rooted && (matches == null || matches.isEmpty())) {
            return project.files();
        }
        FileCollection collection;
        if (classifier != null) {
            collection = resolvedCollections.get(classifier);
            if (collection == null) {
                collection = getFilesForArtifact(classifier, rooted, matches, excludes);
                resolvedCollections.put(classifier, collection);
            }
        } else {
            collection = project.files();
        }
        return collection;
    }

    public abstract Property<String> getVersion();

    public abstract Property<String> getGroupId();

    public abstract Property<String> getArtifactId();

    public abstract Property<String> getExt();

    public abstract Property<String> getHeaderClassifier();

    public abstract Property<String> getSourceClassifier();

    public abstract Property<Boolean> getSharedUsedAtRuntime();

    public abstract SetProperty<String> getSharedPlatforms();

    public abstract SetProperty<String> getStaticPlatforms();

    @Override
    public boolean appliesTo(NativeBinarySpec binary) {
        return true;
    }

    @Override
    public void disallowChanges() {
        hasBeenFinalized = true;
    }

    @Override
    public void disallowUnsafeRead() {
        disallowUnsafeRead = true;
    }

    @Override
    public void finalizeValue() {
        hasBeenFinalized = true;
    }

    @Override
    public void finalizeValueOnRead() {
        finalizeOnRead = true;
    }

}
