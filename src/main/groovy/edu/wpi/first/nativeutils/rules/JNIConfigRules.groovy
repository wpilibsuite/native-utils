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
import org.gradle.api.Project
import edu.wpi.first.nativeutils.NativeUtils
import edu.wpi.first.nativeutils.tasks.JNIHeaders
import edu.wpi.first.nativeutils.tasks.JNISymbolCheck
import edu.wpi.first.nativeutils.dependencysets.JNISourceDependencySet
import edu.wpi.first.nativeutils.dependencysets.JNISystemDependencySet
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import org.gradle.nativeplatform.NativeBinarySpec
import edu.wpi.first.nativeutils.configs.BuildConfig
import org.gradle.platform.base.BinarySpec
import java.util.Properties
import groovy.transform.CompileStatic
import org.gradle.api.tasks.Exec

@SuppressWarnings("GroovyUnusedDeclaration")
class JNIConfigRules extends RuleSource {

    @Validate
    @CompileStatic
    void validateJniConfigHasSourceSets(JNIConfigSpec configs) {
        configs.each { config->
            if (config.onlyIncludeSystemHeaders) {
                return
            }
            assert config.sourceSets != null && config.sourceSets.size() > 0
        }
    }

    @Validate
    @CompileStatic
    void validateJniConfigHasJNIClasses(JNIConfigSpec configs) {
        configs.each { config->
            if (config.onlyIncludeSystemHeaders) {
                return
            }
            assert config.jniDefinitionClasses != null && config.jniDefinitionClasses.size() > 0
        }
    }

    @Mutate
    @CompileStatic
    void createJniTasks(ModelMap<Task> tasks, JNIConfigSpec jniConfigs, ProjectLayout projectLayout,
                        BinaryContainer binaries, BuildTypeContainer buildTypes, BuildConfigSpec configs) {
        def project = (Project)projectLayout.projectIdentifier
        jniConfigs.each { jniConfig->
            JNIHeaders headersTask = null
            Closure getJniSymbols = null
            if (!jniConfig.onlyIncludeSystemHeaders) {
                def generatedJNIHeaderLoc = "${project.buildDir}/${jniConfig.name}/jniinclude"
                def headerTaskName = "${jniConfig.name}jniHeaders"
                tasks.create(headerTaskName, JNIHeaders) {
                    def outputFolder = (File)project.file(generatedJNIHeaderLoc)
                    def createdTask = it;
                    jniConfig.sourceSets.each {
                        createdTask.inputs.files it.output
                    }
                    createdTask.outputs.dir outputFolder
                    createdTask.doLast {
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
                            def execTask = (Exec)it
                            execTask.executable org.gradle.internal.jvm.Jvm.current().getExecutable('javah')

                            execTask.args '-d', outputFolder
                            execTask.args '-classpath', classPath
                            jniConfig.jniDefinitionClasses.each {
                                execTask.args it
                            }
                        }
                    }
                }

                headersTask = (JNIHeaders)tasks.get(headerTaskName)

                getJniSymbols = {
                    def symbolsList = []

                    headersTask.outputs.files.each {
                        FileTree tree = project.fileTree(dir: it)
                        tree.each { File file ->
                            file.eachLine { line ->
                                if (line.trim()) {
                                    if (line.startsWith("JNIEXPORT ") && line.contains('JNICALL')) {
                                        def split = line.split('JNICALL').collect { ((String)it).trim() }
                                        // p2 is our JNI call
                                        symbolsList << split[1]
                                    }
                                }
                            }
                        }
                    }

                    return symbolsList
                }

            }

            configs.findAll { BuildConfigRulesBase.isConfigEnabled((BuildConfig)it, projectLayout.projectIdentifier) }.each { oConfig ->
                binaries.findAll {
                        if (!BuildConfigRulesBase.isNativeProject((BinarySpec)it)) {
                            return false;
                        }
                        NativeBinarySpec spec = (NativeBinarySpec)it
                        return (spec.component.name == jniConfig.name || spec.component.name == "${jniConfig.name}Test".toString())
                    }.each { oBinary ->
                    NativeBinarySpec binary = (NativeBinarySpec)oBinary
                    BuildConfig config = (BuildConfig)oConfig;
                    if (binary.targetPlatform.architecture.name == config.architecture
                        && binary.targetPlatform.operatingSystem.name == config.operatingSystem
                        && binary.targetPlatform.operatingSystem.name != 'windows'
                        && binary instanceof SharedLibraryBinarySpec
                        && !jniConfig.skipSymbolCheck
                        && !jniConfig.onlyIncludeSystemHeaders) {
                        def input = binary.buildTask.name
                        def checkTaskName = 'check' + input.substring(0, 1).toUpperCase() + input.substring(1) + "JniSymbols";
                        tasks.create(checkTaskName, JNISymbolCheck) {
                            JNISymbolCheck createdTask = (JNISymbolCheck)it;
                            SharedLibraryBinarySpec slbs = (SharedLibraryBinarySpec)binary
                            createdTask.dependsOn slbs.tasks.link
                            createdTask.inputs.file(binary.sharedLibraryFile)
                            createdTask.doLast {
                                def library = binary.sharedLibraryFile.absolutePath

                                OutputStream nmOutput = new ByteArrayOutputStream()
                                project.exec {
                                    def execTask = (Exec)it
                                    execTask.commandLine BuildConfigRulesBase.binTools('nm', projectLayout, config), library
                                    execTask.standardOutput = nmOutput
                                }
                                // Remove '\r' so we can check for full string contents
                                def nmSymbols = nmOutput.toString().replace('\r', '')

                                def symbolList = getJniSymbols()

                                def missingSymbols = []

                                symbolList.each {
                                    //Add \n so we can check for the exact symbol
                                    String symbol = (String)it;
                                    def found = nmSymbols.contains(symbol + '\n')
                                    if (!found) {
                                        missingSymbols.add(symbol);
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
                            jniFiles.add("${jdkLocation}/include".toString())
                            if (binary.targetPlatform.operatingSystem.macOsX) {
                                jniFiles.add("${jdkLocation}/include/darwin".toString())
                            } else if (binary.targetPlatform.operatingSystem.linux) {
                                jniFiles.add("${jdkLocation}/include/linux".toString())
                            } else if (binary.targetPlatform.operatingSystem.windows) {
                                jniFiles.add("${jdkLocation}/include/win32".toString())
                            } else if (binary.targetPlatform.operatingSystem.freeBSD) {
                                jniFiles.add("${jdkLocation}/include/freebsd".toString())
                            } else if (project.file("$jdkLocation/include/darwin").exists()) {
                                // TODO: As of Gradle 2.8, targetPlatform.operatingSystem.macOsX returns false
                                // on El Capitan. We therefore manually test for the darwin folder and include it
                                // if it exists
                                jniFiles.add("${jdkLocation}/include/darwin".toString())
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
                                ((AbstractNativeSourceCompileTask)it).dependsOn headersTask
                            }
                        }
                    }
                }
            }
        }
    }
}
