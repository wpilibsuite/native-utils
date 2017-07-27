package edu.wpi.first.nativeutils.dependencysets

import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeDependencySet
import org.gradle.nativeplatform.NativeBinarySpec

public abstract class WPINativeDependencySet implements NativeDependencySet {
    protected String m_rootLocation
    protected NativeBinarySpec m_binarySpec
    protected Project m_project
    protected String m_libraryName

    public WPINativeDependencySet(String rootLocation, NativeBinarySpec binarySpec, String libraryName, Project project) {
        m_rootLocation = rootLocation
        m_binarySpec = binarySpec
        m_libraryName = libraryName
        m_project = project
    }

    public FileCollection getIncludeRoots() {
        return m_project.files("${m_rootLocation}/headers")
    }
}
