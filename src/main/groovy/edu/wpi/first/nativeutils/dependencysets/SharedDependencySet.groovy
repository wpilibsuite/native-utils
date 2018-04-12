package edu.wpi.first.nativeutils.dependencysets

import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeBinarySpec
import edu.wpi.first.nativeutils.NativeUtils
import groovy.transform.InheritConstructors
import groovy.transform.CompileStatic

@InheritConstructors
@CompileStatic
public class SharedDependencySet extends WPINativeDependencySet {
    @CompileStatic
    private FileCollection getFiles(boolean isRuntime) {
        def classifier = NativeUtils.getClassifier(m_binarySpec)
        def platformPath = NativeUtils.getPlatformPath(m_binarySpec)
        def dirPath = 'shared'

        def fileList =  m_project.fileTree("${m_rootLocation}/${classifier}/${platformPath}/${dirPath}/").filter { ((File)it).isFile() }
        if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows' && !isRuntime) {
            fileList = fileList.filter { it.toString().endsWith('.lib') }
        } else if (m_binarySpec.targetPlatform.operatingSystem.name == 'windows') {
            fileList = fileList.filter { it.toString().endsWith('.dll') }
        } else {
            fileList = fileList.filter { (it.toString().endsWith('.so') || it.toString().endsWith('.dylib') || (it.toString().contains('.so.') && !it.toString().endsWith('.debug')))  }
        }

        return m_project.files(fileList.files)
    }
    @CompileStatic
    public FileCollection getLinkFiles() {
        return getFiles(false)
    }
    @CompileStatic
    public FileCollection getRuntimeFiles() {
        return getFiles(true)
    }
}
