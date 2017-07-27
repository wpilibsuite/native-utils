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
import org.gradle.model.*
import org.gradle.nativeplatform.BuildTypeContainer
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeExecutableSpec
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
import org.gradle.platform.base.PlatformContainer
import edu.wpi.first.nativeutils.configs.*
import edu.wpi.first.nativeutils.NativeUtils

interface BuildConfigSpec extends ModelMap<BuildConfig> {}

interface DependencyConfigSpec extends ModelMap<DependencyConfig> {}

interface ExportsConfigSpec extends ModelMap<ExportsConfig> {}

interface JNIConfigSpec extends ModelMap<JNIConfig> {}

@SuppressWarnings("GroovyUnusedDeclaration")
class BuildConfigRules extends RuleSource {

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Model('buildConfigs')
    void createBuildConfigs(BuildConfigSpec configs) {}

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Model('dependencyConfigs')
    void createDependencyConfigs(DependencyConfigSpec configs) {}

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Model('jniConfigs')
    void createJniConfigs(JNIConfigSpec configs) {}

    @Model('exportsConfigs')
    void createExportsConfigs(ExportsConfigSpec configs) {}

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Validate
    void validateCompilerFamilyExists(BuildConfigSpec configs) {
        configs.each { config ->
            assert config.compilerFamily == 'VisualCpp' ||
                    config.compilerFamily == 'Gcc' ||
                    config.compilerFamily == 'Clang'
        }
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    @Validate
    void validateOsExists(BuildConfigSpec configs) {
        def validOs = ['windows', 'osx', 'linux', 'unix']
        configs.each { config ->
            assert validOs.contains(config.operatingSystem.toLowerCase())
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Validate
    void setTargetPlatforms(ComponentSpecContainer components, ProjectLayout projectLayout, BuildConfigSpec configs) {
        components.each { component ->
            configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { config ->
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
    void addBuildTypes(BuildTypeContainer buildTypes, ProjectLayout projectLayout) {
        if (projectLayout.projectIdentifier.hasProperty('releaseBuild')) {
            buildTypes.create('release')
        } else {
            println 'Currently building a debug binary. To build release, add -PreleaseBuild to your gradle command.'
            buildTypes.create('debug')
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void disableCrossCompileGoogleTest(BinaryContainer binaries, BuildConfigSpec configs) {
        def crossCompileConfigs = configs.findAll { BuildConfigRulesBase.isCrossCompile(it) }.collect { it.architecture }
        if (crossCompileConfigs != null && !crossCompileConfigs.empty) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { spec ->
                if (crossCompileConfigs.contains(spec.targetPlatform.architecture.name)) {
                    spec.buildable = false
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void setSkipGoogleTest(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def skipConfigs = configs.findAll { it.skipTests }.collect { it.architecture + ':' + it.operatingSystem }
        if (skipConfigs != null && !skipConfigs.empty) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { spec ->
                def checkString = spec.targetPlatform.architecture.name + ':' + spec.targetPlatform.operatingSystem.name
                if (skipConfigs.contains(checkString)) {
                    spec.buildable = false
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void setSkipAllGoogleTest(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def skipAllTests = projectLayout.projectIdentifier.hasProperty('skipAllTests')
        if (skipAllTests) {
            binaries.withType(GoogleTestTestSuiteBinarySpec) { spec ->
                    spec.buildable = false
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void createStripTasks(ModelMap<Task> tasks, BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        def project = projectLayout.projectIdentifier
        configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { config ->
            binaries.findAll { BuildConfigRulesBase.isNativeProject(it) }.each { binary ->
                if (binary.targetPlatform.architecture.name == config.architecture
                    && binary.targetPlatform.operatingSystem.name == config.operatingSystem
                    && ((config.debugStripBinaries && binary.buildType.name.contains('debug')) ||  (config.releaseStripBinaries && binary.buildType.name.contains('release')))
                    && binary.targetPlatform.operatingSystem.name != 'windows'
                    && binary instanceof SharedLibraryBinarySpec) {
                    def input = binary.buildTask.name
                    def task = binary.tasks.link
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
                                project.exec { commandLine BuildConfigRulesBase.binTools('objcopy', projectLayout, config), '--only-keep-debug', library, debugLibrary }
                                project.exec { commandLine BuildConfigRulesBase.binTools('strip', projectLayout, config), '-g', library }
                                project.exec { commandLine BuildConfigRulesBase.binTools('objcopy', projectLayout, config), "--add-gnu-debuglink=$debugLibrary", library }
                            }
                        }
                    }
                }
            }
        }
    }

    @Mutate
    void createInstallAllComponentsTask(ModelMap<Task> tasks, ComponentSpecContainer components) {
        tasks.create("installAllExecutables") {
            components.each { component->
                if (component in NativeExecutableSpec) {
                    component.binaries.each { binary->
                        dependsOn binary.tasks.install
                    }
                }
            }
        }
    }

    @SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
    @Mutate
    void createPlatforms(PlatformContainer platforms, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }

        configs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { config ->
            if (config.architecture != null) {
                platforms.create(config.architecture) { platform ->
                    platform.architecture config.architecture
                    if (config.operatingSystem != null) {
                        platform.operatingSystem config.operatingSystem
                    }
                }
            }
        }
    }

    @Validate
    void setDebugToolChainArgs(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }

        def enabledConfigs = configs.findAll {
            BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) && (it.debugCompilerArgs != null || it.debugLinkerArgs != null)
        }
        if (enabledConfigs == null || enabledConfigs.empty) {
            return
        }

        binaries.findAll {
            BuildConfigRulesBase.isNativeProject(it) && it.buildType.name.contains('debug') }.each { binary ->
            def config = enabledConfigs.find {
                it.architecture == binary.targetPlatform.architecture.name &&
                        BuildConfigRulesBase.getCompilerFamily(it.compilerFamily).isAssignableFrom(binary.toolChain.class)
            }
            if (config != null) {
                BuildConfigRulesBase.addArgsToTool(binary.cppCompiler, config.debugCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.linker, config.debugLinkerArgs)
            }
        }
    }

    @Validate
    void setReleaseToolChainArgs(BinaryContainer binaries, ProjectLayout projectLayout, BuildConfigSpec configs) {
        if (configs == null) {
            return
        }

        def enabledConfigs = configs.findAll {
            BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) && (it.releaseCompilerArgs != null || it.releaseLinkerArgs != null)
        }
        if (enabledConfigs == null || enabledConfigs.empty) {
            return
        }

        binaries.findAll {
            BuildConfigRulesBase.isNativeProject(it) && it.buildType.name.contains('release') }.each { binary ->
            def config = enabledConfigs.find {
                it.architecture == binary.targetPlatform.architecture.name &&
                        BuildConfigRulesBase.getCompilerFamily(it.compilerFamily).isAssignableFrom(binary.toolChain.class)
            }
            if (config != null) {
                BuildConfigRulesBase.addArgsToTool(binary.cppCompiler, config.releaseCompilerArgs)
                BuildConfigRulesBase.addArgsToTool(binary.linker, config.releaseLinkerArgs)
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