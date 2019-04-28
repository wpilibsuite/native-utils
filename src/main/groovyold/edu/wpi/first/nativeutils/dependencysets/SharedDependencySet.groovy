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
public class SharedDependencySet extends WPINativeDependencySet {

    @CompileStatic
    @Override
    protected FileCollection getFiles(boolean isRuntime) {
        def platformPath = NativeUtils.getPlatformPath(m_binarySpec)
        def dirPath = 'shared'

        List<String> matchers = [];
        List<String> excludes = [];

        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows' && !isRuntime) {
            matchers << "**/*${platformPath}/${dirPath}/*.pdb".toString()
        } else {
            matchers << "**/*${platformPath}/${dirPath}/*.so.debug".toString()
            matchers << "**/*${platformPath}/${dirPath}/*.so.*.debug".toString()
        }

        def debugFiles = m_libs.matching { PatternFilterable pat ->
            pat.include(matchers)
            pat.exclude(excludes)
        }
        matchers = [];
        excludes = [];

        if (!isRuntime) {
            excludes.addAll(m_linkExcludes)
        }

        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows' && !isRuntime) {
            matchers << "**/*${platformPath}/${dirPath}/*.lib".toString()
        } else if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows') {
            matchers << "**/*${platformPath}/${dirPath}/*.dll".toString()
        } else {
            matchers << "**/*${platformPath}/${dirPath}/*.dylib".toString()
            matchers << "**/*${platformPath}/${dirPath}/*.so".toString()
            matchers << "**/*${platformPath}/${dirPath}/*.so.*".toString()
            excludes << "**/*${platformPath}/${dirPath}/*.so.debug".toString()
            excludes << "**/*${platformPath}/${dirPath}/*.so.*.debug".toString()
        }

        def sharedFiles = m_libs.matching { PatternFilterable pat ->
            pat.include(matchers)
            pat.exclude(excludes)
        }

        if (isRuntime) {
            return m_project.files(sharedFiles.files + debugFiles.files)
        } else {
            return m_project.files(sharedFiles.files)
        }
    }
}
