package edu.wpi.first.nativeutils.dependencies.configs;

import java.io.File;
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
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

public abstract class WPIMavenDependency implements NativeDependency, HasConfigurableValue {
    private boolean finalizeOnRead = false;
    private boolean hasBeenFinalized = false;
    private boolean disallowUnsafeRead = false;
    private final String name;
    private final Project project;

    private Callable<DirectoryTree> headers;
    private final ProviderFactory providerFactory;

    @Inject
    public WPIMavenDependency(String name, Project project, ProviderFactory providerFactory) {
        this.name = name;
        this.project = project;
        this.providerFactory = providerFactory;

        headers = getFilesForArtifact(getHeaderClassifier());
    }

    private Callable<DirectoryTree> getFilesForArtifact(Provider<String> classifier) {
        String configName = name + "_" + classifier;
        Configuration cfg = project.getConfigurations().create(configName);
        //provider.provider(arg0)
        Provider<String> mavenConfig = providerFactory.provider(() -> {
            String dep = getGroupId().get() + ":" + getArtifactId().get() + ":" + getVersion().get() + ":" + classifier.get()
                    + "@" + getExt().get();
            System.out.println(dep);
            return dep;
        });
        project.getDependencies().add(configName, mavenConfig);
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
        return new Callable<DirectoryTree>() {
            @Override
            public DirectoryTree call() {
                return (DirectoryTree)project.fileTree(includeDirs.getFiles().getSingleFile());
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NativeDependencySet getNativeDependencySet(NativeBinarySpec binary) {
        // Check if we've been finalized
        if (!hasBeenFinalized && disallowUnsafeRead) {
            // TODO proper exceptions
            throw new GradleException("Value not finalized, unsafe read");
        }
        if (finalizeOnRead) {
            hasBeenFinalized = true;
        }

        return new NativeDependencySet() {

            @Override
            public FileCollection getIncludeRoots() {
                Callable<File> cbl = () -> headers.call().getDir();
                return project.files(cbl);
            }

            @Override
            public FileCollection getLinkFiles() {
                return project.files();
            }

            @Override
            public FileCollection getRuntimeFiles() {
                return project.files();
            }

        };
    }

    public abstract Property<String> getVersion();

    public abstract Property<String> getGroupId();

    public abstract Property<String> getArtifactId();

    public abstract Property<String> getExt();

    public abstract Property<String> getHeaderClassifier();

    public abstract Property<String> getSourceClassifier();

    public abstract Property<Boolean> getSharedUsedAtRuntime();

    public abstract ListProperty<String> getSharedPlatforms();

    public abstract ListProperty<String> getStaticPlatforms();

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
