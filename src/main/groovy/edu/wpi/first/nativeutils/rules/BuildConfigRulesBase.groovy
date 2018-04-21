package edu.wpi.first.nativeutils.rules

import org.gradle.internal.os.OperatingSystem
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectIdentifier
import org.gradle.model.*
import org.gradle.nativeplatform.BuildTypeContainer
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.Tool
import org.gradle.nativeplatform.toolchain.Clang
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath
import org.gradle.nativeplatform.toolchain.internal.ToolType
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.platform.base.BinaryContainer
import org.gradle.platform.base.BinarySpec
import org.gradle.platform.base.ComponentSpecContainer
import org.gradle.platform.base.PlatformContainer
import edu.wpi.first.nativeutils.configs.BuildConfig
import edu.wpi.first.nativeutils.NativeUtils
import edu.wpi.first.nativeutils.configs.CrossBuildConfig
import groovy.transform.CompileStatic

class ConfigEnables {
    public boolean configEnabled = false
}

class BuildConfigRulesBase {
    private static final ToolSearchPath toolSearchPath = new ToolSearchPath(OperatingSystem.current())

    @CompileStatic
    static String binTools(String tool, ProjectLayout projectLayout, BuildConfig config) {
        def toolChainPath = NativeUtils.getToolChainPath(config, (Project) projectLayout.projectIdentifier)
        def compilerPrefix = config.toolChainPrefix
        if (compilerPrefix == null) compilerPrefix = ''
        if (toolChainPath != null) return "${toolChainPath}/${compilerPrefix}${tool}"
        return "${compilerPrefix}${tool}"
    }

    @CompileStatic
    static void addArgsToTool(Tool tool, args) {
        if (args != null) {
            tool.args.addAll((List<String>) args)
        }
    }

    @CompileStatic
    static Class getCompilerFamily(String family) {
        switch (family) {
            case 'VisualCpp':
                return VisualCpp
            case 'Gcc':
                return Gcc
            case 'Clang':
                return Clang
        }
    }

    @CompileStatic
    static boolean isNativeProject(BinarySpec binary) {
        return binary instanceof NativeBinarySpec
    }

    @CompileStatic
    static boolean isCrossCompile(BuildConfig config) {
        return config instanceof CrossBuildConfig
    }

    @CompileStatic
    static boolean isComponentEnabled(BuildConfig config, String componentName) {
        if (config.exclude == null || config.exclude.size() == 0) {
            return true
        }
        return !config.exclude.contains(componentName)
    }

    private static final Map<BuildConfig, ConfigEnables> configEnabledMap = [:]

    /**
     * If a config is crosscompiling, only enable for athena. Otherwise, only enable if the current os is the config os,
     * or specific cross compiler is specified
     */
    @CompileStatic
    static boolean isConfigEnabled(BuildConfig config, Project project) {
        if (configEnabledMap.containsKey(config)) {
            return configEnabledMap.get(config).configEnabled
        }

        ConfigEnables enable = new ConfigEnables()
        configEnabledMap.put(config, enable);

        if (isCrossCompile(config) && NativeUtils.getCrossConfigEnabledCmdLine((CrossBuildConfig) config, project)) {
            enable.configEnabled = doesToolChainExist(config, project)
            return enable.configEnabled
        }
        if (!config.detectPlatform) {
            enable.configEnabled = false
            return enable.configEnabled
        }

        def detect = config.detectPlatform

        enable.configEnabled = detect(config)
        return enable.configEnabled
    }

    private static final Map<BuildConfig, Boolean> existingToolChains = [:]

    @CompileStatic
    static boolean doesToolChainExist(BuildConfig config, Project project) {
        if (!isCrossCompile(config)) {
            return true;
        }

        if (existingToolChains.containsKey(config)) {
            return existingToolChains.get(config)
        }

        def path = NativeUtils.getToolChainPath(config, project)
        def toolPath = path == null ? "" : path

        boolean foundToolChain = toolSearchPath.locate(ToolType.CPP_COMPILER, toolPath + config.toolChainPrefix + "g++").isAvailable()

        existingToolChains.put(config, foundToolChain)

        return foundToolChain
    }
}
