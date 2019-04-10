package edu.wpi.first.nativeutils.dependencysets;

import java.io.File;
import java.util.List;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.nativeplatform.NativeDependencySet;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.nativeplatform.NativeBinarySpec;
import edu.wpi.first.nativeutils.NativeUtils;
import edu.wpi.first.nativeutils.NativeUtilsExtension;

import org.gradle.api.internal.file.collections.FileSystemMirroringFileTree;
import org.gradle.api.internal.file.collections.FileTreeAdapter;
import org.gradle.api.internal.file.collections.LocalFileTree;
import org.gradle.api.internal.file.collections.MinimalFileTree;

public abstract class WPINativeDependencySet implements NativeDependencySet {
    protected NativeBinarySpec m_binarySpec;
    protected NativeUtilsExtension m_nativeExtension;
    protected FileCollection m_headers;
    protected FileCollection m_source;
    protected FileCollection m_libs;
    protected String m_libConfigName;
    protected String m_headerConfigName;
    protected String m_sourceConfigName;
    protected Project m_project;
    protected List<String> m_linkExcludes;

    public WPINativeDependencySet(NativeBinarySpec binarySpec, NativeUtilsExtension nativeExtension, String headers, String libConfigName, String src, Project project, List<String> linkExcludes) {
        m_binarySpec = binarySpec;
        m_headerConfigName = headers;
        m_libConfigName = libConfigName;
        m_project = project;
        m_sourceConfigName = src;
        m_linkExcludes = linkExcludes;
        m_nativeExtension = nativeExtension;
    }

    private void resolveHeaderConfigs() {
        Configuration headerConfig = m_project.getConfigurations().getByName(m_headerConfigName);
        DependencySet dependencies = headerConfig.getDependencies();

        Dependency first = dependencies.iterator().next();
        File file = headerConfig.files(first).iterator().next();

        FileTree zipTree = m_project.zipTree(file);

        zipTree.visit(FileVisitDetails::getFile);
        MinimalFileTree lTree = ((FileTreeAdapter)zipTree).getTree();
        DirectoryTree dirTree;
        if (lTree instanceof FileSystemMirroringFileTree)
            dirTree = ((FileSystemMirroringFileTree)lTree).getMirror();
        else
            dirTree = ((LocalFileTree)lTree).getLocalContents().iterator().next();

        System.out.println("DirTree " + dirTree);
        System.out.println("DirTreeDir " + dirTree.getDir());
        m_headers = m_project.files(dirTree.getDir());
        //rootDir = dirTree.dir
    }

    @Override
    public FileCollection getIncludeRoots() {
        resolveHeaderConfigs();
        System.out.println(m_headers);

        return m_headers;
    }

    private void resolveLibConfigs() {
        // def libConfig = m_project.configurations.getByName(m_libConfigName)
        // def libZip = m_project.zipTree(libConfig.dependencies.collectMany { libConfig.files(it) as Collection }.first())
        // m_libs = libZip

        Configuration libConfig = m_project.getConfigurations().getByName(m_libConfigName);
        DependencySet dependencies = libConfig.getDependencies();

        Dependency first = dependencies.iterator().next();
        File file = libConfig.files(first).iterator().next();

        //m_libs = m_project.zipTree(file);
        System.out.println(file);
        m_libs = m_project.zipTree(file);
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
            Configuration libConfig = m_project.getConfigurations().getByName(m_sourceConfigName);
            DependencySet dependencies = libConfig.getDependencies();
            m_source = m_project.zipTree(libConfig.files(dependencies.toArray(Dependency[]::new)));
        } else {
            m_source = m_project.files();
        }
    }

    public FileCollection getSourceFiles() {
        resolveSourceConfigs();
        if (m_source.isEmpty()) {
            return m_project.files();
        }
        return m_project.files(m_source.getSingleFile());
    }
}
