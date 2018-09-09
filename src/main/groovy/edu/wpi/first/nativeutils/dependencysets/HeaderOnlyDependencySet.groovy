package edu.wpi.first.nativeutils.dependencysets

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeDependencySet

@CompileStatic
class HeaderOnlyDependencySet implements NativeDependencySet {
    private NativeBinarySpec m_binarySpec
    private FileTree m_headers
    private String m_headerConfigName
    private Project m_project

    HeaderOnlyDependencySet(NativeBinarySpec binarySpec, String headers, Project project) {
        m_binarySpec = binarySpec
        m_headerConfigName = headers
        m_project = project
    }

    private void resolveHeaderConfigs() {
        def headerConfig = m_project.configurations.getByName(m_headerConfigName)
        m_headers = m_project.zipTree(headerConfig.dependencies.collectMany { headerConfig.files(it) as Collection }.first())
    }

    @Override
    @CompileDynamic
    public FileCollection getIncludeRoots() {
        resolveHeaderConfigs()
        return m_project.files(m_headers.asFileTrees.first().dir);
    }

    @Override
    FileCollection getLinkFiles() {
        return m_project.files()
    }

    @Override
    FileCollection getRuntimeFiles() {
        return m_project.files()
    }
}
