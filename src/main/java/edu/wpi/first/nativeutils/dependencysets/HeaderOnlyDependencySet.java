package edu.wpi.first.nativeutils.dependencysets;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeBinarySpec;

public class HeaderOnlyDependencySet extends WPINativeDependencyBase {
    public HeaderOnlyDependencySet(NativeBinarySpec binarySpec, String headers, Project project) {
        super(project, headers);
    }

    @Override
    public FileCollection getLinkFiles() {
        return this.m_emptyCollection;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        return this.m_emptyCollection;
    }
}
