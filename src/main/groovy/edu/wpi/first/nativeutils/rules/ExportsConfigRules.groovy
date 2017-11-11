package edu.wpi.first.nativeutils.rules

import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.model.*
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

@SuppressWarnings("GroovyUnusedDeclaration")
class ExportsConfigRules extends RuleSource {

    @Validate
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

        def project = projectLayout.projectIdentifier

        configs.each { config->
            components.each { component->
                if (component.name == config.name) {
                    // Component matches config
                    if (component instanceof NativeLibrarySpec) {
                        component.binaries.each { binary->
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
                                    objFileDirArr.each {
                                        inputs.dir(it)
                                    }
                                    defFile = project.file(it.getTemporaryDir().toString() + '/exports.def')
                                    outputs.file(defFile)
                                    doLast {
                                        def exeName = NativeUtils.getGeneratorFilePath();
                                        def files = []
                                        objFileDirArr.each {
                                            files.add(project.fileTree(it).include("**/*.obj"))
                                        }
                                        project.exec {
                                            executable = exeName
                                            args defFile
                                            files.each {
                                                args it
                                            }
                                        }

                                        def lines = []
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
                                                lines = config.x86SymbolFilter(lines);
                                            }
                                        } else {
                                            if (config.x64SymbolFilter != null) {
                                                lines = config.x64SymbolFilter(lines);
                                            }
                                        }

                                        defFile.withWriter{ out ->
                                            out.println 'EXPORTS'
                                            lines.each {out.println it}
                                        }
                                    }
                                }

                                def linkTask = binary.tasks.link
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
    void doThingWithExports(ModelMap<Task> tasks, ExportsConfigSpec exports) {}
}
