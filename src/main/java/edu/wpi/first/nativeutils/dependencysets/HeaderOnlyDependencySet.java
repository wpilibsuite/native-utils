package edu.wpi.first.nativeutils.dependencysets;

import java.util.stream.Stream;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeDependencySet;

public class HeaderOnlyDependencySet implements NativeDependencySet {
    private FileCollection m_headers;
    private String m_headerConfigName;
    private Project m_project;

    public HeaderOnlyDependencySet(NativeBinarySpec binarySpec, String headers, Project project) {
        m_headerConfigName = headers;
        m_project = project;
    }

    private void resolveHeaderConfigs() {
        Configuration headerConfig = m_project.getConfigurations().getByName(m_headerConfigName);
        DependencySet dependencies = headerConfig.getDependencies();
        m_headers = m_project.files(m_project.zipTree(headerConfig.files(dependencies.toArray(Dependency[]::new))).getSingleFile());

        //headerConfig.files(arg0)
        //headerConfig.fileCollection(headerConfig.getDependencies());
        //headerConfig.getDependencies().stream().collect(x -> headerConfig.files(it));
        //m_headers = m_project.zipTree(headerConfig.getDependencies().collectMany { headerConfig.files(it) as Collection }.first())
    }

    @Override
    public FileCollection getIncludeRoots() {
        resolveHeaderConfigs();
        return m_headers;
    }

    @Override
    public FileCollection getLinkFiles() {
        return m_project.files();
    }

    @Override
    public FileCollection getRuntimeFiles() {
        return m_project.files();
    }
}
