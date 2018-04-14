package edu.wpi.first.nativeutils.rules

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.Copy
import org.gradle.internal.os.OperatingSystem
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.api.internal.project.ProjectIdentifier
import org.gradle.language.assembler.tasks.Assemble
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import org.gradle.model.*
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.nativeplatform.BuildTypeContainer
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.platform.base.VariantComponentSpec
import org.gradle.nativeplatform.NativeExecutableSpec
import org.gradle.nativeplatform.NativeComponentSpec
import org.gradle.nativeplatform.SharedLibraryBinarySpec
import org.gradle.nativeplatform.StaticLibraryBinarySpec
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec
import org.gradle.nativeplatform.Tool
import org.gradle.nativeplatform.toolchain.Clang
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.platform.base.BinaryContainer
import org.gradle.platform.base.BinarySpec
import org.gradle.platform.base.ComponentSpecContainer
import org.gradle.nativeplatform.tasks.LinkSharedLibrary
import org.gradle.platform.base.PlatformContainer
import org.gradle.nativeplatform.TargetedNativeComponent
import edu.wpi.first.nativeutils.configs.*
import edu.wpi.first.nativeutils.NativeUtils
import edu.wpi.first.nativeutils.tasks.NativeInstallAll
import org.gradle.nativeplatform.NativeExecutableBinarySpec
import org.gradle.platform.base.ComponentSpec
import org.gradle.nativeplatform.platform.NativePlatform
import groovy.transform.CompileStatic

interface BuildConfigSpec extends ModelMap<BuildConfig> {}

interface DependencyConfigSpec extends ModelMap<DependencyConfig> {}

@SuppressWarnings("GroovyUnusedDeclaration")
class BuildConfigRules extends RuleSource {

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Model('buildConfigs')
    @CompileStatic
    void createBuildConfigs(BuildConfigSpec configs) {}

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Model('dependencyConfigs')
    @CompileStatic
    void createDependencyConfigs(DependencyConfigSpec configs) {}

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Validate
    @CompileStatic
    void validateCompilerFamilyExists(BuildConfigSpec configs) {
        for (BuildConfig config : configs) {
            assert config.compilerFamily == 'VisualCpp' ||
                   config.compilerFamily == 'Gcc' ||
                   config.compilerFamily == 'Clang'
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Validate
    @CompileStatic
    void validateOsExists(BuildConfigSpec configs) {
        def validOs = ['windows', 'osx', 'linux', 'unix']
        for (BuildConfig config : configs) {
            assert validOs.contains(config.operatingSystem.toLowerCase())
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Validate
    @CompileStatic
    void setTargetPlatforms(ComponentSpecContainer components, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def project = (Project)projectLayout.projectIdentifier
        for (ComponentSpec oComponent : components) {
            if (!(oComponent in TargetedNativeComponent)) {
                continue
            }
            for (BuildConfig config : configs) {
                if (!BuildConfigRulesBase.isConfigEnabled(config, project)) {
                    continue
                }
                TargetedNativeComponent component = (TargetedNativeComponent)oComponent
                if (config.include == null || config.include.size() == 0) {
                    if (config.exclude == null || !config.exclude.contains(component.name)) {
                        component.targetPlatform config.architecture
                    }
                } else {
                    if (config.include.contains(component.name)) {
                        component.targetPlatform config.architecture
                    }
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    @CompileStatic
    void addBuildTypes(BuildTypeContainer buildTypes, ProjectLayout projectLayout) {
        def project =  (Project)projectLayout.projectIdentifier
        if (project.hasProperty('releaseBuild')) {
            buildTypes.create('release')
        } else {
            println 'Currently building a debug binary. To build release, add -PreleaseBuild to your gradle command.'
            buildTypes.create('debug')
        }
    }

    private void setBuildableFalseDynamically(NativeBinarySpec binary) {
        binary.buildable = false
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    @CompileStatic
    void disableCrossCompileGoogleTest(BinaryContainer binaries, BuildConfigSpec configs) {
        def crossCompileConfigs = []
        for (BuildConfig config : configs) {
            if (!BuildConfigRulesBase.isCrossCompile(config)) {
                continue
            }
            crossCompileConfigs << config.architecture
        }
        if (!crossCompileConfigs.empty) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { oSpec ->
                GoogleTestTestSuiteBinarySpec spec = (GoogleTestTestSuiteBinarySpec)oSpec
                if (crossCompileConfigs.contains(spec.targetPlatform.architecture.name)) {
                    setBuildableFalseDynamically(spec)
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    @CompileStatic
    void setSkipGoogleTest(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def skipConfigs = []
        for (BuildConfig config : configs) {
            if (!config.skipTests) {
                continue
            }
            skipConfigs << config.architecture + ':' + config.operatingSystem
        }
        if (!skipConfigs.empty) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { oSpec ->
                GoogleTestTestSuiteBinarySpec spec = (GoogleTestTestSuiteBinarySpec)oSpec
                def checkString = spec.targetPlatform.architecture.name + ':' + spec.targetPlatform.operatingSystem.name
                if (skipConfigs.contains(checkString)) {
                    setBuildableFalseDynamically(spec)
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    @CompileStatic
    void setSkipAllGoogleTest(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def project = (Project)projectLayout.projectIdentifier
        def skipAllTests = project.hasProperty('skipAllTests')
        if (skipAllTests) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { spec ->
                    setBuildableFalseDynamically((GoogleTestTestSuiteBinarySpec)spec)
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void createStripTasks(ModelMap<Task> tasks, BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def project = (Project)projectLayout.projectIdentifier
        for (BuildConfig config : configs) {
            if (!BuildConfigRulesBase.isConfigEnabled(config, project)) {
                continue
            }
            for (BinarySpec oBinary : binaries) {
                if (!BuildConfigRulesBase.isNativeProject(oBinary)) {
                    continue
                }
                NativeBinarySpec binary = (NativeBinarySpec)oBinary
                if (binary.targetPlatform.architecture.name == config.architecture
                    && binary.targetPlatform.operatingSystem.name == config.operatingSystem
                    && ((config.debugStripBinaries && binary.buildType.name.contains('debug')) ||  (config.releaseStripBinaries && binary.buildType.name.contains('release')))
                    && binary.targetPlatform.operatingSystem.name != 'windows'
                    && binary instanceof SharedLibraryBinarySpec) {
                    def sBinary = (SharedLibraryBinarySpec)binary
                    def task = (LinkSharedLibrary)sBinary.tasks.link
                    def lockConfig = config
                    if (binary.targetPlatform.operatingSystem.name == 'osx') {

                        def library = task.outputFile.absolutePath
                        task.doLast {
                            if (new File(library).exists()) {
                                project.exec { commandLine "dsymutil", library }
                                project.exec { commandLine "strip", '-S', library }
                            }
                        }
                    } else {
                        def library = task.outputFile.absolutePath
                        def debugLibrary = task.outputFile.absolutePath + ".debug"
                        task.doLast {
                            if (new File(library).exists()) {
                                project.exec { commandLine BuildConfigRulesBase.binTools('objcopy', projectLayout, lockConfig), '--only-keep-debug', library, debugLibrary }
                                project.exec { commandLine BuildConfigRulesBase.binTools('strip', projectLayout, lockConfig), '-g', library }
                                project.exec { commandLine BuildConfigRulesBase.binTools('objcopy', projectLayout, lockConfig), "--add-gnu-debuglink=$debugLibrary", library }
                            }
                        }
                    }
                }
            }
        }
    }

    @Mutate
    @CompileStatic
    void createInstallAllComponentsTask(ModelMap<Task> tasks, ComponentSpecContainer components) {
        tasks.create("installAllExecutables", NativeInstallAll) {
            Task task = (Task)it
            task.group = 'Install'
            task.description = 'Install all executables from this project'
            for (ComponentSpec oComponent : components) {
                if (oComponent in NativeExecutableSpec) {
                    NativeExecutableSpec component = (NativeExecutableSpec)oComponent
                    for (BinarySpec binary : component.binaries) {
                        def install = ((NativeExecutableBinarySpec)binary).tasks.install
                        task.dependsOn install
                    }
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    @CompileStatic
    void createPlatforms(PlatformContainer platforms, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }
        def project = (Project)projectLayout.projectIdentifier
        for (BuildConfig config : configs) {
            if (!BuildConfigRulesBase.isConfigEnabled(config, project)) {
                continue
            }
            if (config.architecture != null) {
                platforms.create(config.architecture) { oPlatform ->
                    NativePlatform platform = (NativePlatform)oPlatform
                    platform.architecture config.architecture
                    if (config.operatingSystem != null) {
                        platform.operatingSystem config.operatingSystem
                    }
                }
            }
        }
    }

    @Validate
    @CompileStatic
    void setDebugToolChainArgs(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }
        def project = (Project)projectLayout.projectIdentifier

        def enabledConfigs = []
        for (BuildConfig cfg : configs) {
            if (BuildConfigRulesBase.isConfigEnabled(cfg, project) && (cfg.debugCompilerArgs != null || cfg.debugLinkerArgs != null)) {
                enabledConfigs << cfg
            }
        }

        if (enabledConfigs.empty) {
            return
        }

        for (BinarySpec oBinary : binaries) {
            if (!BuildConfigRulesBase.isNativeProject(oBinary)) {
                continue
            }
            def binary = (NativeBinarySpec)oBinary
            if (!binary.buildType.name.contains('debug')) {
                continue
            }

            BuildConfig config = null
            for (BuildConfig it : enabledConfigs) {
                if (it.architecture == binary.targetPlatform.architecture.name &&
                        BuildConfigRulesBase.getCompilerFamily(it.compilerFamily).isAssignableFrom(binary.toolChain.class)) {
                    config = it
                    break;
                }
            }
            if (config != null) {
                BuildConfigRulesBase.addArgsToTool(binary.cppCompiler, config.debugCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.cCompiler, config.debugCCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.linker, config.debugLinkerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.assembler, config.debugAsmCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.objcCompiler, config.debugObjCCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.objcppCompiler, config.debugObjCppCompilerArgs)
            }
        }
    }

    @Validate
    @CompileStatic
    void setReleaseToolChainArgs(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }

        def project = (Project)projectLayout.projectIdentifier

        def enabledConfigs = []
        for (BuildConfig cfg : configs) {
            if (BuildConfigRulesBase.isConfigEnabled(cfg, project) && (cfg.releaseCompilerArgs != null || cfg.releaseLinkerArgs != null)) {
                enabledConfigs << cfg
            }
        }

        if (enabledConfigs.empty) {
            return
        }

        for (BinarySpec oBinary : binaries) {
            if (!BuildConfigRulesBase.isNativeProject(oBinary)) {
                continue
            }
            def binary = (NativeBinarySpec)oBinary
            if (!binary.buildType.name.contains('release')) {
                continue
            }

            BuildConfig config = null
            for (BuildConfig it : enabledConfigs) {
                if (it.architecture == binary.targetPlatform.architecture.name &&
                        BuildConfigRulesBase.getCompilerFamily(it.compilerFamily).isAssignableFrom(binary.toolChain.class)) {
                    config = it
                    break;
                }
            }
            if (config != null) {
                BuildConfigRulesBase.addArgsToTool(binary.cppCompiler, config.releaseCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.cCompiler, config.releaseCCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.linker, config.releaseLinkerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.assembler, config.releaseAsmCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.objcCompiler, config.releaseObjCCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.objcppCompiler, config.releaseObjCppCompilerArgs)
            }
        }
    }

    @CompileStatic
    static void performWarningPrinting(String taskName, Project project) {
        def warningFile = project.file("$project.buildDir/tmp/$taskName/output.txt");

        if (!warningFile.exists()) {
            return
        }

        def currentFile = ''
        def hasFirstLine = false

        def hasPrintedFileName = false
        warningFile.eachLine { line->
            if (!hasFirstLine) {
                hasFirstLine = true
                return
            }
            if (line.startsWith('compiling ')) {
                currentFile = line.substring(10, line.indexOf('successful.'))
                hasPrintedFileName = false
                return
            }
            if (line.contains('Finished') && line.contains('see full log')) {
                return
            }

            if (line.trim().equals(currentFile.trim())) {
                return
            }

            if (!line.isEmpty()) {
                if (!hasPrintedFileName) {
                    hasPrintedFileName = true
                    println "Warnings in file $currentFile"
                }
                println line
            }

        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Validate
    @CompileStatic
    void setupCompilerWarningPrinting(ModelMap<Task> tasks, ProjectLayout projectLayout, ComponentSpecContainer components) {

        if (components == null) {
            return
        }

        def project = (Project)projectLayout.projectIdentifier

        if (project.hasProperty('skipWarningPrints')) {
            return
        }

        for (ComponentSpec component : components) {
            if (component instanceof NativeLibrarySpec || component instanceof NativeExecutableSpec) {
                for (BinarySpec binary : ((VariantComponentSpec)component).binaries) {
                    binary.tasks.withType(AbstractNativeSourceCompileTask) {
                        def task = (AbstractNativeSourceCompileTask)it
                        task.doLast {
                            BuildConfigRules.performWarningPrinting(task.name.toString(), project)
                        }
                    }
                }
            }

        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Mutate
    void createToolChains(NativeToolChainRegistry toolChains, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }

        def vcppConfigs = configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) && it.compilerFamily == 'VisualCpp' }
        if (vcppConfigs != null && !vcppConfigs.empty) {
            toolChains.create('visualCpp', VisualCpp.class) { t ->
                t.eachPlatform { toolChain ->
                    def config = vcppConfigs.find { it.architecture == toolChain.platform.architecture.name }
                    if (config != null) {
                        if (config.toolChainPrefix != null) {
                            toolChain.cCompiler.executable = config.toolChainPrefix + toolChain.cCompiler.executable
                            toolChain.cppCompiler.executable = config.toolChainPrefix + toolChain.cppCompiler.executable
                            toolChain.linker.executable = config.toolChainPrefix + toolChain.linker.executable
                            toolChain.assembler.executable = config.toolChainPrefix + toolChain.assembler.executable
                            toolChain.staticLibArchiver.executable = config.toolChainPrefix + toolChain.staticLibArchiver.executable
                        }
                        if (config.compilerArgs != null) {
                            toolChain.cppCompiler.withArguments { args ->
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.CCompilerArgs != null) {
                            cCompiler.withArguments { args ->
                                config.CCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCCompilerArgs != null) {
                            objcCompiler.withArguments { args ->
                                config.objCCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCppCompilerArgs != null) {
                            objcppCompiler.withArguments { args ->
                                config.objCppCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.asmCompilerArgs != null) {
                            assembler.withArguments { args ->
                                config.asmCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            toolChain.linker.withArguments { args ->
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }

        def gccConfigs = configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) && it.compilerFamily == 'Gcc' }
        if (gccConfigs != null && !gccConfigs.empty) {
            toolChains.create('gcc', Gcc.class) {
                gccConfigs.each { config ->
                    target(config.architecture) {
                        def gccToolPath = NativeUtils.getToolChainPath(config, projectLayout.projectIdentifier)
                        if (gccToolPath == null) {
                            gccToolPath = ""
                        }
                        if (config.toolChainPrefix != null) {
                            cCompiler.executable = gccToolPath + config.toolChainPrefix + cCompiler.executable
                            cppCompiler.executable = gccToolPath + config.toolChainPrefix + cppCompiler.executable
                            linker.executable = gccToolPath + config.toolChainPrefix + linker.executable
                            assembler.executable = gccToolPath + config.toolChainPrefix + assembler.executable
                            staticLibArchiver.executable = gccToolPath + config.toolChainPrefix + staticLibArchiver.executable
                        }

                        if (config.compilerArgs != null) {
                            cppCompiler.withArguments { args ->
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.CCompilerArgs != null) {
                            cCompiler.withArguments { args ->
                                config.CCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCCompilerArgs != null) {
                            objcCompiler.withArguments { args ->
                                config.objCCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCppCompilerArgs != null) {
                            objcppCompiler.withArguments { args ->
                                config.objCppCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.asmCompilerArgs != null) {
                            assembler.withArguments { args ->
                                config.asmCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            linker.withArguments { args ->
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }

        def clangConfigs = configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) && it.compilerFamily == 'Clang' }
        if (clangConfigs != null && !clangConfigs.empty) {
            toolChains.create('clang', Clang.class) {
                clangConfigs.each { config ->
                    target(config.architecture) {
                        if (config.toolChainPrefix != null) {
                            cCompiler.executable = config.toolChainPrefix + cCompiler.executable
                            cppCompiler.executable = config.toolChainPrefix + cppCompiler.executable
                            linker.executable = config.toolChainPrefix + linker.executable
                            assembler.executable = config.toolChainPrefix + assembler.executable
                            staticLibArchiver.executable = config.toolChainPrefix + staticLibArchiver.executable
                        }

                        if (config.compilerArgs != null) {
                            cppCompiler.withArguments { args ->
                                config.compilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.CCompilerArgs != null) {
                            cCompiler.withArguments { args ->
                                config.CCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCCompilerArgs != null) {
                            objcCompiler.withArguments { args ->
                                config.objCCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.objCppCompilerArgs != null) {
                            objcppCompiler.withArguments { args ->
                                config.objCppCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.asmCompilerArgs != null) {
                            assembler.withArguments { args ->
                                config.asmCompilerArgs.each { a -> args.add(a) }
                            }
                        }
                        if (config.linkerArgs != null) {
                            linker.withArguments { args ->
                                config.linkerArgs.each { a -> args.add(a) }
                            }
                        }
                    }
                }
            }
        }
    }
}
