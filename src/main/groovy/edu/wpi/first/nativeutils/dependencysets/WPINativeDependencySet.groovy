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
    protected Project m_project

    public WPINativeDependencySet(NativeBinarySpec binarySpec, FileTree headers, FileTree libs, FileTree src, Project project) {
        m_binarySpec = binarySpec
        m_headers = headers
        m_libs = libs
        m_project = project
        m_source = src
    }

    @Override
    @CompileDynamic
    public FileCollection getIncludeRoots() {
        return m_project.files(m_headers.asFileTrees.first().dir);
    }

    protected abstract FileCollection getFiles(boolean isRuntime, boolean isDebug)

    @Override
    public FileCollection getLinkFiles() {
        return getFiles(false, false)
    }

    @Override
    public FileCollection getRuntimeFiles() {
        return getFiles(true, false)
    }

    public FileCollection getDebugFiles() {
        return getFiles(true, true);
    }

    @CompileDynamic
    public FileCollection getSourceFiles() {
        def asFileTree = m_source.asFileTrees
        if (asFileTree.empty) {
            return m_project.files()
        }
        return m_project.files(asFileTree.first().dir);
    }
}
