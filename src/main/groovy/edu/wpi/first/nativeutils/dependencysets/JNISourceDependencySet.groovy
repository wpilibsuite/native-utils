package edu.wpi.first.nativeutils.dependencysets

import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeDependencySet
import org.gradle.nativeplatform.NativeBinarySpec

public class JNISourceDependencySet implements NativeDependencySet {
    protected List<String> m_jniHeaders
    protected Project m_project

    public JNISourceDependencySet(List<String> jniHeaders, Project project) {
      m_jniHeaders = jniHeaders
      m_project = project
    }

    public FileCollection getIncludeRoots() {
        return m_project.files(m_jniHeaders)
    }

    public FileCollection getLinkFiles() {
        return m_project.files()
    }

    public FileCollection getRuntimeFiles() {
        return m_project.files()
    }
}
