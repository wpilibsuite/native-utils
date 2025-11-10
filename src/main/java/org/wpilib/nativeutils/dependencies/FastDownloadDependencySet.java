package org.wpilib.nativeutils.dependencies;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeDependencySet;

import edu.wpi.first.vscode.dependencies.SourceContainingNativeDependencySet;

public class FastDownloadDependencySet implements NativeDependencySet, SourceContainingNativeDependencySet {

    private final FileCollection emptyCollection;

    private final Configuration headerConfiguration;
    private final Configuration sourcesConfiguration;
    private final Configuration runtimeConfiguration;
    private final Configuration linkConfiguration;

    public void addConfiguration(ArtifactType type, Configuration configuration) {
        switch (type) {
        case SOURCES -> sourcesConfiguration.extendsFrom(configuration);
        case HEADERS -> headerConfiguration.extendsFrom(configuration);
        case LINK -> linkConfiguration.extendsFrom(configuration);
        case RUNTIME -> runtimeConfiguration.extendsFrom(configuration);
        }
    }

    public FastDownloadDependencySet(String binaryName, Project project) {
        emptyCollection = project.files();
        headerConfiguration = project.getConfigurations().create(binaryName + "_uberheaders");
        sourcesConfiguration = project.getConfigurations().create(binaryName + "_ubersources");
        linkConfiguration = project.getConfigurations().create(binaryName + "_uberlink");
        runtimeConfiguration = project.getConfigurations().create(binaryName + "_uberruntime");
    }

    @Override
    public FileCollection getSourceRoots() {
        sourcesConfiguration.getIncoming().getFiles().getFiles();
        return emptyCollection;
    }

    @Override
    public FileCollection getIncludeRoots() {
        headerConfiguration.getIncoming().getFiles().getFiles();
        return emptyCollection;
    }

    @Override
    public FileCollection getLinkFiles() {
        linkConfiguration.getIncoming().getFiles().getFiles();
        return emptyCollection;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        runtimeConfiguration.getIncoming().getFiles().getFiles();
        return emptyCollection;
    }

}
