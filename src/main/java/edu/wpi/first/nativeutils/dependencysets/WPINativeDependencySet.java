package edu.wpi.first.nativeutils.dependencysets;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeBinarySpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;

public abstract class WPINativeDependencySet extends WPINativeDependencyBase {
    protected NativeBinarySpec m_binarySpec;
    protected NativeUtilsExtension m_nativeExtension;
    protected FileCollection m_source;
    protected FileCollection m_libs;
    protected String m_libConfigName;
    protected String m_headerConfigName;
    protected String m_sourceConfigName;
    protected List<String> m_linkExcludes;

    public WPINativeDependencySet(NativeBinarySpec binarySpec, NativeUtilsExtension nativeExtension, String headers, String libConfigName, String src, Project project, List<String> linkExcludes) {
        super(project, headers);
        m_binarySpec = binarySpec;
        m_headerConfigName = headers;
        m_libConfigName = libConfigName;
        m_sourceConfigName = src;
        m_linkExcludes = linkExcludes;
        m_nativeExtension = nativeExtension;
    }

    private void resolveLibConfigs() {
        m_libs = m_project.fileTree(extractDependency(m_libConfigName, m_libConfigName));
    }

    protected abstract FileCollection getFiles(boolean isRuntime);

    @Override
    public FileCollection getLinkFiles() {
        resolveLibConfigs();
        return getFiles(false);
    }

    @Override
    public FileCollection getRuntimeFiles() {
        resolveLibConfigs();
        return getFiles(true);
    }

    private void resolveSourceConfigs() {
        if (m_sourceConfigName != null) {
            m_source = m_project.files(extractDependency(m_sourceConfigName, m_sourceConfigName));
        } else {
            m_source = m_project.files();
        }
    }

    public FileCollection getSourceFiles() {
        resolveSourceConfigs();
        return m_source;
    }
}
