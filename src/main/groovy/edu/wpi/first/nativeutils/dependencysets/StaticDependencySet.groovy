package edu.wpi.first.nativeutils.dependencysets

import java.io.File
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeDependencySet
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.nativeplatform.NativeBinarySpec
import groovy.transform.InheritConstructors
import groovy.transform.CompileStatic
import edu.wpi.first.nativeutils.NativeUtils

@CompileStatic
@InheritConstructors
public class StaticDependencySet extends WPINativeDependencySet {

    @CompileStatic
    @Override
    protected FileCollection getFiles(boolean isRuntime) {
        if (isRuntime) {
            return m_project.files();
        }

        def platformPath = NativeUtils.getPlatformPath(m_binarySpec)
        def dirPath = 'static'

        List<String> matchers = [];
        List<String> excludes = [];

        excludes.addAll(m_linkExcludes)

        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows') {
            matchers << "**/*${platformPath}/${dirPath}/*.lib".toString()
        } else {
            matchers << "**/*${platformPath}/${dirPath}/*.a".toString()
        }

        def staticFiles = m_libs.matching { PatternFilterable pat ->
            pat.include(matchers)
            pat.exclude(excludes)
        }

        return m_project.files(staticFiles.files)
    }
}
