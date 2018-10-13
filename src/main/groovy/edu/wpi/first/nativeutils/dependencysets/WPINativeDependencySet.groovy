package edu.wpi.first.nativeutils.dependencysets

import java.io.File
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeDependencySet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.nativeplatform.NativeBinarySpec
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import edu.wpi.first.nativeutils.NativeUtils

@CompileStatic
public abstract class WPINativeDependencySet implements NativeDependencySet {
    protected NativeBinarySpec m_binarySpec
    protected FileTree m_headers
    protected FileTree m_source
    protected FileTree m_libs
    protected String m_libConfigName
    protected String m_headerConfigName
    protected String m_sourceConfigName
    protected Project m_project
    protected List<String> m_linkExcludes

    public WPINativeDependencySet(NativeBinarySpec binarySpec, String headers, String libConfigName, String src, Project project, List<String> linkExcludes) {
        m_binarySpec = binarySpec
        m_headerConfigName = headers
        m_libConfigName = libConfigName
        m_project = project
        m_sourceConfigName = src
        m_linkExcludes = linkExcludes
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

    private void resolveLibConfigs() {
        def libConfig = m_project.configurations.getByName(m_libConfigName)
        def libZip = m_project.zipTree(libConfig.dependencies.collectMany { libConfig.files(it) as Collection }.first())
        m_libs = libZip
    }

    protected abstract FileCollection getFiles(boolean isRuntime, boolean isDebug)

    @Override
    public FileCollection getLinkFiles() {
        resolveLibConfigs()
        return getFiles(false, false)
    }

    @Override
    public FileCollection getRuntimeFiles() {
        resolveLibConfigs()
        return getFiles(true, false)
    }

    public FileCollection getDebugFiles() {
        resolveLibConfigs()
        return getFiles(true, true);
    }

    private void resolveSourceConfigs() {
        if (m_sourceConfigName != null) {
            def sourceConfig = m_project.configurations.getByName(m_sourceConfigName)
            m_source = m_project.zipTree(sourceConfig.dependencies.collectMany { sourceConfig.files(it) as Collection }.first())
        } else {
            m_source = m_project.files().asFileTree
        }
    }

    @CompileDynamic
    public FileCollection getSourceFiles() {
        resolveSourceConfigs()
        def asFileTree = m_source.asFileTrees
        if (asFileTree.empty) {
            return m_project.files()
        }
        return m_project.files(asFileTree.first().dir);
    }
}
