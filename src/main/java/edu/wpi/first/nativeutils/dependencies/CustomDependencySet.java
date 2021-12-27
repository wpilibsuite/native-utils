package edu.wpi.first.nativeutils.dependencies;

import javax.inject.Inject;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.vscode.dependencies.SourceContainingNativeDependencySet;

public class CustomDependencySet implements NativeDependencySet, SourceContainingNativeDependencySet {
    private final ConfigurableFileCollection includeRoots;
    private final ConfigurableFileCollection linkFiles;
    private final ConfigurableFileCollection runtimeFiles;
    private final ConfigurableFileCollection sourceRoots;

    @Inject
    public CustomDependencySet(ObjectFactory objects) {
        includeRoots = objects.fileCollection();
        linkFiles = objects.fileCollection();
        runtimeFiles = objects.fileCollection();
        sourceRoots = objects.fileCollection();
    }


    @Override
    public ConfigurableFileCollection getIncludeRoots() {
        return includeRoots;
    }

    @Override
    public ConfigurableFileCollection getLinkFiles() {
        return linkFiles;
    }

    @Override
    public ConfigurableFileCollection getRuntimeFiles() {
        return runtimeFiles;
    }

    @Override
    public ConfigurableFileCollection getSourceRoots() {
        return sourceRoots;
    }

}
