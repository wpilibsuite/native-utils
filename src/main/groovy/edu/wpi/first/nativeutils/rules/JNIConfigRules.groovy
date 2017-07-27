package edu.wpi.first.nativeutils.rules

import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.model.*
import org.gradle.nativeplatform.BuildTypeContainer
import org.gradle.nativeplatform.SharedLibraryBinarySpec
import org.gradle.platform.base.BinaryContainer
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.api.file.FileTree
import edu.wpi.first.nativeutils.NativeUtils

@SuppressWarnings("GroovyUnusedDeclaration")
class JNIConfigRules extends RuleSource {

    @Validate
    void validateJniConfigHasSourceSets(JNIConfigSpec configs) {
        configs.each { config->
            assert config.sourceSets != null && config.sourceSets.size() > 0
        }
    }

    @Validate
    void validateJniConfigHasJNIClasses(JNIConfigSpec configs) {
        configs.each { config->
            assert config.jniDefinitionClasses != null && config.jniDefinitionClasses.size() > 0
        }
    }

    @Mutate
    void createJniTasks(ModelMap<Task> tasks, JNIConfigSpec jniConfigs, ProjectLayout projectLayout,
                        BinaryContainer binaries, BuildTypeContainer buildTypes, BuildConfigSpec configs) {
        def project = projectLayout.projectIdentifier
        jniConfigs.each { jniConfig->
            def generatedJNIHeaderLoc = "${project.buildDir}/${jniConfig.name}/jniinclude"
            def headerTaskName = "${jniConfig.name}jniHeaders"
            tasks.create(headerTaskName) {
                def outputFolder = project.file(generatedJNIHeaderLoc)
                jniConfig.sourceSets.each {
                    inputs.files it.output
                }
                outputs.file outputFolder
                doLast {
                    outputFolder.mkdirs()
                    def classPath = StringBuilder.newInstance()
                    jniConfig.sourceSets.each {
                        it.output.classesDirs.each {
                            classPath << it
                            classPath << ';'
                        }
                    }
                    project.exec {
                        executable org.gradle.internal.jvm.Jvm.current().getExecutable('javah')

                        args '-d', outputFolder
                        args '-classpath', classPath
                        jniConfig.jniDefinitionClasses.each {
                            args it
                        }
                    }
                }
            }

            def headersTask = tasks.get(headerTaskName)

            def getJniSymbols = {
                def symbolsList = []

                headersTask.outputs.files.each {
                    FileTree tree = project.fileTree(dir: it)
                    tree.each { File file ->
                        file.eachLine { line ->
                            if (line.trim()) {
                                if (line.startsWith("JNIEXPORT ") && line.contains('JNICALL')) {
                                    def (p1, p2) = line.split('JNICALL').collect { it.trim() }
                                    // p2 is our JNI call
                                    symbolsList << p2
                                }
                            }
                        }
                    }
                }

                return symbolsList
            }

            configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { config ->
                binaries.findAll { BuildConfigRulesBase.isNativeProject(it)  && (it.component.name == jniConfig.name || it.component.name == "${jniConfig.name}Test".toString()) }.each { binary ->
                    if (binary.targetPlatform.architecture.name == config.architecture
                        && binary.targetPlatform.operatingSystem.name == config.operatingSystem
                        && binary.targetPlatform.operatingSystem.name != 'windows'
                        && binary instanceof SharedLibraryBinarySpec
                        && !jniConfig.skipSymbolCheck) {
                        def input = binary.buildTask.name
                        def checkTaskName = 'check' + input.substring(0, 1).toUpperCase() + input.substring(1) + "JniSymbols";
                        tasks.create(checkTaskName) {
                            doLast {
                                def library = binary.sharedLibraryFile.absolutePath
                                def nmOutput = "${BuildConfigRulesBase.binTools('nm', projectLayout, config)} ${library}".execute().text

                                def nmSymbols = nmOutput.toString().replace('\r', '')

                                def symbolList = getJniSymbols()

                                symbolList.each {
                                    //Add \n so we can check for the exact symbol
                                    def found = nmSymbols.contains(it + '\n')
                                    if (!found) {
                                        throw new GradleException("Found a definition that does not have a matching symbol ${it}")
                                    }
                                }
                            }
                        }
                        binary.checkedBy tasks.get(checkTaskName)
                    }

                    if (binary.targetPlatform.architecture.name == config.architecture
                    && binary.targetPlatform.operatingSystem.name == config.operatingSystem ) {

                        if (BuildConfigRulesBase.isCrossCompile(config)) {
                            if (jniConfig.jniArmHeaderLocations != null && jniConfig.jniArmHeaderLocations.size() == 1 && jniConfig.jniArmHeaderLocations.containsKey('all')) {
                                binary.cppCompiler.args '-I', jniConfig.jniArmHeaderLocations.get('all').absolutePath
                                binary.cppCompiler.args '-I', jniConfig.jniArmHeaderLocations.get('all').absolutePath + '/linux'
                            } else if (jniConfig.jniArmHeaderLocations != null && jniConfig.jniArmHeaderLocations.containsKey(config.architecture)) {
                                binary.cppCompiler.args '-I', jniConfig.jniArmHeaderLocations.get(config.architecture).absolutePath
                                binary.cppCompiler.args '-I', jniConfig.jniArmHeaderLocations.get(config.architecture).absolutePath + '/linux'
                            }
                        } else {
                            def jdkLocation = org.gradle.internal.jvm.Jvm.current().javaHome
                            NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include", binary.cppCompiler)
                            if (binary.targetPlatform.operatingSystem.macOsX) {
                                NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include/darwin", binary.cppCompiler)
                            } else if (binary.targetPlatform.operatingSystem.linux) {
                                NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include/linux", binary.cppCompiler)
                            } else if (binary.targetPlatform.operatingSystem.windows) {
                                NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include/win32", binary.cppCompiler)
                            } else if (binary.targetPlatform.operatingSystem.freeBSD) {
                                NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include/freebsd", binary.cppCompiler)
                            } else if (file("$jdkLocation/include/darwin").exists()) {
                                // TODO: As of Gradle 2.8, targetPlatform.operatingSystem.macOsX returns false
                                // on El Capitan. We therefore manually test for the darwin folder and include it
                                // if it exists
                                NativeUtils.setPlatformSpecificIncludeFlag("${jdkLocation}/include/darwin", binary.cppCompiler)
                            }
                        }
                        headersTask.outputs.files.each { file ->
                            if (BuildConfigRulesBase.isCrossCompile(config)) {
                                binary.cppCompiler.args '-I', file.getPath()
                            } else {
                                NativeUtils.setPlatformSpecificIncludeFlag(file.getPath(), binary.cppCompiler)
                            }
                        }

                        binary.tasks.withType(CppCompile) {
                            it.dependsOn headersTask
                        }
                    }
                }
            }
        }
    }
}