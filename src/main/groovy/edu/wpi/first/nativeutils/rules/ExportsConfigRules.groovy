package edu.wpi.first.nativeutils.rules

import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.model.*
import org.gradle.api.Project
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.nativeplatform.BuildTypeContainer
import org.gradle.nativeplatform.SharedLibraryBinarySpec
import org.gradle.platform.base.BinaryContainer
import org.gradle.platform.base.ComponentSpecContainer
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask
import org.gradle.api.file.FileTree
import org.gradle.internal.os.OperatingSystem
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import edu.wpi.first.nativeutils.NativeUtils
import groovy.transform.CompileStatic
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeComponentSpec
import org.gradle.api.tasks.Exec

@SuppressWarnings("GroovyUnusedDeclaration")
class ExportsConfigRules extends RuleSource {

    @Validate
    @CompileStatic
    void setupExports(ModelMap<Task> tasks, ExportsConfigSpec configs, ProjectLayout projectLayout, ComponentSpecContainer components) {
        if (!OperatingSystem.current().isWindows()) {
            return
        }

        if (configs == null) {
            return
        }

        if (components == null) {
            return
        }

        def project = (Project)projectLayout.projectIdentifier

        configs.each { config->
            components.each { oComponent ->
                NativeComponentSpec component = (NativeComponentSpec)oComponent
                if (component.name == config.name) {
                    // Component matches config
                    if (component instanceof NativeLibrarySpec) {
                        ((NativeLibrarySpec)component).binaries.each { oBinary->
                            NativeBinarySpec binary = (NativeBinarySpec)oBinary
                            def excludeBuildTypes = config.excludeBuildTypes == null ? [] : config.excludeBuildTypes
                            if (binary.targetPlatform.operatingSystem.name == 'windows'
                                && binary instanceof SharedLibraryBinarySpec
                                && !excludeBuildTypes.contains(binary.buildType.name)) {

                                def taskArray = []
                                def objFileDirArr = []

                                binary.tasks.withType(AbstractNativeSourceCompileTask).each {
                                    objFileDirArr.add(it.objectFileDir)
                                    taskArray.add(it)
                                }

                                def defFile

                                def exportsTaskName = 'generateExports' + binary.buildTask.name

                                def exportsTask = project.tasks.create(exportsTaskName, ExportsGenerationTask) {
                                    def createdTask = (ExportsGenerationTask)it
                                    objFileDirArr.each {
                                        createdTask.inputs.dir(it)
                                    }
                                    def tmpDir = project.file("$project.buildDir/tmp/$exportsTaskName")
                                    defFile = project.file(tmpDir.toString() + '/exports.def')
                                    createdTask.outputs.file(defFile)
                                    createdTask.doLast {
                                        tmpDir.mkdirs()
                                        def exeName = NativeUtils.getGeneratorFilePath();
                                        def files = []
                                        objFileDirArr.each {
                                            files.add(project.fileTree(it).include("**/*.obj"))
                                        }
                                        project.exec {
                                            def execTask = (Exec)it
                                            execTask.executable = exeName
                                            execTask.args defFile
                                            files.each {
                                                execTask.args it
                                            }
                                        }

                                        List<String> lines = []
                                        def excludeSymbols
                                        if (binary.targetPlatform.architecture.name == 'x86') {
                                            excludeSymbols = config.x86ExcludeSymbols
                                        } else {
                                            excludeSymbols = config.x64ExcludeSymbols
                                        }

                                        if (excludeSymbols == null) {
                                            excludeSymbols = []
                                        }

                                        defFile.eachLine { line->

                                            def symbol = line.trim()
                                            def space = symbol.indexOf(' ')
                                            if (space != -1) {
                                                symbol = symbol.substring(0, space)
                                            }
                                            if (symbol != 'EXPORTS' && !excludeSymbols.contains(symbol)) {
                                                lines << symbol
                                            }
                                        }

                                        if (binary.targetPlatform.architecture.name == 'x86') {
                                            if (config.x86SymbolFilter != null) {
                                                def filter = config.x86SymbolFilter;
                                                lines = (List<String>)filter(lines);
                                            }
                                        } else {
                                            if (config.x64SymbolFilter != null) {
                                                def filter = config.x64SymbolFilter;
                                                lines = (List<String>)filter(lines);
                                            }
                                        }

                                        defFile.withWriter{ out ->
                                            out.println 'EXPORTS'
                                            lines.each {out.println it}
                                        }
                                    }
                                }

                                def linkTask = ((SharedLibraryBinarySpec)binary).tasks.link
                                binary.linker.args "/DEF:${defFile}"
                                taskArray.each {
                                    exportsTask.dependsOn it
                                }
                                linkTask.dependsOn exportsTask
                                linkTask.inputs.file(defFile)
                            }
                        }
                    }
                }
            }
        }
    }

    @Mutate
    @CompileStatic
    void doThingWithExports(ModelMap<Task> tasks, ExportsConfigSpec exports) {}
}
