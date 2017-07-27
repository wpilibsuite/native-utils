package edu.wpi.first.nativeutils.dependencysets

import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeBinarySpec
import edu.wpi.first.nativeutils.NativeUtils
import groovy.transform.InheritConstructors

@InheritConstructors
public class StaticDependencySet extends WPINativeDependencySet {
    public FileCollection getLinkFiles() {
        def classifier = NativeUtils.getClassifier(m_binarySpec)
        def platformPath = NativeUtils.getPlatformPath(m_binarySpec)
        def dirPath = 'static'

        def fileList =  m_project.fileTree("${m_rootLocation}/${classifier}/${platformPath}/${dirPath}/").filter { it.isFile() }
        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows') {
            fileList = fileList.filter { it.toString().endsWith('.lib') }
        } else {
            fileList = fileList.filter { it.toString().endsWith('.a') }
        }


        return m_project.files(fileList.files)
    }

    public FileCollection getRuntimeFiles() {
        return m_project.files()
    }
}