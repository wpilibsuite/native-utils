package edu.wpi.first.nativeutils.dependencies;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeDependencySet;

public class FastDownloadDependencySet implements NativeDependencySet {

    private final FileCollection emptyCollection;
    private final List<Configuration> headerConfigurations = new ArrayList<>();
    private final List<Configuration> linkConfigurations = new ArrayList<>();
    private final List<Configuration> runtimeConfiguration = new ArrayList<>();
    private final List<Configuration> sourcesConfigurations = new ArrayList<>();

    public void addConfiguration(ArtifactType type, Configuration configuration) {
        switch (type) {
        case SOURCES:
            break;
        case HEADERS:
            break;
        case LINK:
            break;
        case RUNTIME:
            break;

        default:
            break;
        }
    }

    public void addHeaderConfiguration(Configuration configuration) {

    }

    public void addLinkConfiguration(Configuration configuration) {

    }

    public void addRuntimeConfiguration(Configuration configuration) {

    }

    public void addSourcesConfiguration(Configuration configuration) {

    }

    public FastDownloadDependencySet(Project project) {
        emptyCollection = project.files();
    }

    // Called getSourceFiles called by reflection in gradle-cpp-vscode
    public FileCollection getSourceFiles() {
        return emptyCollection;
    }

    @Override
    public FileCollection getIncludeRoots() {
        return emptyCollection;
    }

    @Override
    public FileCollection getLinkFiles() {
        return emptyCollection;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        return emptyCollection;
    }

}
