package edu.wpi.first.nativeutils.dependencysets

import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeBinarySpec
import edu.wpi.first.nativeutils.NativeUtils
import groovy.transform.InheritConstructors

@InheritConstructors
public class SharedDependencySet extends WPINativeDependencySet {

    private FileCollection getFiles(boolean isRuntime) {
        def classifier = NativeUtils.getClassifier(m_binarySpec)
        def platformPath = NativeUtils.getPlatformPath(m_binarySpec)
        def dirPath = 'shared'

        def fileList =  m_project.fileTree("${m_rootLocation}/${classifier}/${platformPath}/${dirPath}/").filter { it.isFile() }
        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows' && !isRuntime) {
            fileList = fileList.filter { it.toString().endsWith('.lib') }
        } else if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows') {
            fileList = fileList.filter { it.toString().endsWith('.dll') }
        } else {
            fileList = fileList.filter { it.toString().endsWith('.so') }
        }

        return m_project.files(fileList.files)
    }

    public FileCollection getLinkFiles() {
        return getFiles(false)
    }

    public FileCollection getRuntimeFiles() {
        return getFiles(true)
    }
}
