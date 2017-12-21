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
import edu.wpi.first.nativeutils.tasks.JNIHeaders
import edu.wpi.first.nativeutils.tasks.JNISymbolCheck
import edu.wpi.first.nativeutils.dependencysets.JNISourceDependencySet
import edu.wpi.first.nativeutils.dependencysets.JNISystemDependencySet
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import java.util.Properties

@SuppressWarnings("GroovyUnusedDeclaration")
class JNIConfigRules extends RuleSource {

    @Validate
    void validateJniConfigHasSourceSets(JNIConfigSpec configs) {
        configs.each { config->
            if (config.onlyIncludeSystemHeaders) {
                return
            }
            assert config.sourceSets != null && config.sourceSets.size() > 0
        }
    }

    @Validate
    void validateJniConfigHasJNIClasses(JNIConfigSpec configs) {
        configs.each { config->
            if (config.onlyIncludeSystemHeaders) {
                return
            }
            assert config.jniDefinitionClasses != null && config.jniDefinitionClasses.size() > 0
        }
    }

    @Mutate
    void createJniTasks(ModelMap<Task> tasks, JNIConfigSpec jniConfigs, ProjectLayout projectLayout,
                        BinaryContainer binaries, BuildTypeContainer buildTypes, BuildConfigSpec configs) {
        def project = projectLayout.projectIdentifier
        jniConfigs.each { jniConfig->
            def headersTask = null
            def getJniSymbols = null
            if (!jniConfig.onlyIncludeSystemHeaders) {
                def generatedJNIHeaderLoc = "${project.buildDir}/${jniConfig.name}/jniinclude"
                def headerTaskName = "${jniConfig.name}jniHeaders"
                tasks.create(headerTaskName, JNIHeaders) {
                    def outputFolder = project.file(generatedJNIHeaderLoc)
                    jniConfig.sourceSets.each {
                        inputs.files it.output
                    }
                    outputs.dir outputFolder
                    doLast {
                        outputFolder.mkdirs()
                        def classPath = StringBuilder.newInstance()
                        jniConfig.sourceSets.each {
                            it.output.classesDirs.each {
                                classPath << it
                                classPath << System.getProperty("path.separator");
                            }
                        }
                        classPath.deleteCharAt(classPath.length()-1)

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

                headersTask = tasks.get(headerTaskName)

                getJniSymbols = {
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

            }

            configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { config ->
                binaries.findAll { BuildConfigRulesBase.isNativeProject(it)  && (it.component.name == jniConfig.name || it.component.name == "${jniConfig.name}Test".toString()) }.each { binary ->
                    if (binary.targetPlatform.architecture.name == config.architecture
                        && binary.targetPlatform.operatingSystem.name == config.operatingSystem
                        && binary.targetPlatform.operatingSystem.name != 'windows'
                        && binary instanceof SharedLibraryBinarySpec
                        && !jniConfig.skipSymbolCheck
                        && !jniConfig.onlyIncludeSystemHeaders) {
                        def input = binary.buildTask.name
                        def checkTaskName = 'check' + input.substring(0, 1).toUpperCase() + input.substring(1) + "JniSymbols";
                        tasks.create(checkTaskName, JNISymbolCheck) {
                            dependsOn binary.tasks.link
                            inputs.file(binary.sharedLibraryFile)
                            doLast {
                                def library = binary.sharedLibraryFile.absolutePath

                                def nmOutput = new ByteArrayOutputStream()
                                project.exec {
                                    commandLine BuildConfigRulesBase.binTools('nm', projectLayout, config), library
                                    standardOutput nmOutput
                                }
                                // Remove '\r' so we can check for full string contents
                                def nmSymbols = nmOutput.toString().replace('\r', '')

                                def symbolList = getJniSymbols()

                                def missingSymbols = []

                                symbolList.each {
                                    //Add \n so we can check for the exact symbol
                                    def found = nmSymbols.contains(it + '\n')
                                    if (!found) {
                                        missingSymbols.add(it);
                                    }
                                }

                                if (missingSymbols.size() != 0) {
                                    def missingString = StringBuilder.newInstance()
                                    missingSymbols.each {
                                        missingString << it
                                        missingString << '\n'
                                    }
                                    throw new GradleException("Found a definition that does not have a matching symbol ${missingString.toString()}")
                                }
                            }
                        }
                        binary.checkedBy tasks.get(checkTaskName)
                    }

                    if (binary.targetPlatform.architecture.name == config.architecture
                    && binary.targetPlatform.operatingSystem.name == config.operatingSystem ) {

                        List<String> jniFiles = []

                        if (BuildConfigRulesBase.isCrossCompile(config)) {
                            if (jniConfig.jniArmHeaderLocations != null && jniConfig.jniArmHeaderLocations.size() == 1 && jniConfig.jniArmHeaderLocations.containsKey('all')) {
                                jniFiles.add(jniConfig.jniArmHeaderLocations.get('all').absolutePath)
                                jniFiles.add(jniConfig.jniArmHeaderLocations.get('all').absolutePath + '/linux')
                            } else if (jniConfig.jniArmHeaderLocations != null && jniConfig.jniArmHeaderLocations.containsKey(config.architecture)) {
                                jniFiles.add(jniConfig.jniArmHeaderLocations.get('all').absolutePath)
                                jniFiles.add(jniConfig.jniArmHeaderLocations.get('all').absolutePath + '/linux')
                            }
                        } else {
                            def jdkLocation = org.gradle.internal.jvm.Jvm.current().javaHome
                            jniFiles.add("${jdkLocation}/include")
                            if (binary.targetPlatform.operatingSystem.macOsX) {
                                jniFiles.add("${jdkLocation}/include/darwin")
                            } else if (binary.targetPlatform.operatingSystem.linux) {
                                jniFiles.add("${jdkLocation}/include/linux")
                            } else if (binary.targetPlatform.operatingSystem.windows) {
                                jniFiles.add("${jdkLocation}/include/win32")
                            } else if (binary.targetPlatform.operatingSystem.freeBSD) {
                                jniFiles.add("${jdkLocation}/include/freebsd")
                            } else if (file("$jdkLocation/include/darwin").exists()) {
                                // TODO: As of Gradle 2.8, targetPlatform.operatingSystem.macOsX returns false
                                // on El Capitan. We therefore manually test for the darwin folder and include it
                                // if it exists
                                jniFiles.add("${jdkLocation}/include/darwin")
                            }
                        }

                        binary.lib(new JNISystemDependencySet(jniFiles, project))

                        if (headersTask != null) {
                            List<String> jniHeadersList = []
                            headersTask.outputs.files.each { file ->
                                jniHeadersList.add(file.getPath())
                            }

                            binary.lib(new JNISourceDependencySet(jniHeadersList, project))

                            binary.tasks.withType(AbstractNativeSourceCompileTask) {
                                it.dependsOn headersTask
                            }
                        }
                    }
                }
            }
        }
    }
}
