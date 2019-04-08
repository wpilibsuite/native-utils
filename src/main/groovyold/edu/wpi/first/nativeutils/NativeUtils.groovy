package edu.wpi.first.nativeutils

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.internal.project.ProjectIdentifier
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeLibraryBinarySpec
import org.gradle.nativeplatform.Tool
import edu.wpi.first.nativeutils.configs.BuildConfig
import org.gradle.nativeplatform.StaticLibraryBinarySpec
import edu.wpi.first.nativeutils.rules.BuildConfigRulesBase
import edu.wpi.first.nativeutils.configs.CrossBuildConfig
import groovy.transform.CompileStatic
import edu.wpi.first.nativeutils.tasks.NativeInstallAll
import org.gradle.nativeplatform.plugins.NativeComponentPlugin

/**
 * Created by 333fr on 3/1/2017.
 */
public class NativeUtils implements Plugin<Project> {

    private static final Map<CrossBuildConfig, Boolean> enabledConfigCache = [:]

    @CompileStatic
    public static boolean getCrossConfigEnabledCmdLine(CrossBuildConfig config, Project project) {
        if (!config.skipByDefault) {
            return true
        }

        if (enabledConfigCache.containsKey(config)) {
            return enabledConfigCache.get(config)
        }

        for (item in project.properties) {
            def key = item.key
            def value = item.value
            if (key.contains('-toolChainPath')) {
                String[] configSplit = key.split("-", 2);
                if (value != null && configSplit.length == 2 && configSplit[0] != "") {
                    if (configSplit[0] == config.architecture) {
                        enabledConfigCache.put(config, true)
                        return true;
                    }
                }
            }
        }

        // Try get command line enable
        for (item in project.properties) {
            def key = item.key
            if (key.contains('-enableCross')) {
                String[] configSplit = key.split("-", 2);
                if (configSplit[0] == config.architecture) {
                    enabledConfigCache.put(config, true)
                    return true
                }
            }
        }

        enabledConfigCache.put(config, false)
        return false
    }


    private static final Map<BuildConfig, String> toolChainPathCache = [:]

    private static Object getValue(Object item) {
        return item.value
    }

    /**
     * Gets the toolChainPath for the specific build configuration
     */
    @CompileStatic
    public static String getToolChainPath(BuildConfig config, Project project) {
        if (!(config instanceof CrossBuildConfig)) {
            return null
        }

        if (toolChainPathCache.containsKey(config)) {
            return toolChainPathCache.get(config)
        }
        // Try getting the toolChainPath
        for (item in project.properties) {
            def key = item.key
            def value = getValue(item)
            if (key.contains('-toolChainPath')) {
                String[] configSplit = key.split("-", 2);
                if (value != null && configSplit.length == 2 && configSplit[0] != "") {
                    if (configSplit[0] == config.architecture) {
                        toolChainPathCache.put(config, (String) (Object) value)
                        return value
                    }
                }
            }
        }

        String path = ((CrossBuildConfig) config).toolChainPath
        toolChainPathCache.put(config, path)
        return path
    }

    @CompileStatic
    public static boolean isConfigEnabled(BuildConfig config, Project project) {
        return BuildConfigRulesBase.isConfigEnabled(config, project)
    }

    /**
     * Gets the extraction platform path for the specific build configuration
     */
    @CompileStatic
    public static String getPlatformPath(BuildConfig config) {
        return config.operatingSystem + '/' + config.architecture
    }

    /**
     * Gets the artifact classifier for a specifc build configuration
     */
    @CompileStatic
    public static String getClassifier(BuildConfig config) {
        return config.operatingSystem + config.architecture
    }

    /**
     * Gets the extraction platform path for a specific binary
     */
    @CompileStatic
    public static String getPlatformPath(NativeBinarySpec binary) {
        return binary.targetPlatform.operatingSystem.name + '/' + binary.targetPlatform.architecture.name
    }

    /**
     * Gets the artifact classifier for a specific binary
     */
    @CompileStatic
    public static String getClassifier(NativeBinarySpec binary) {
        def classifierBase = binary.targetPlatform.operatingSystem.name + binary.targetPlatform.architecture.name
        if (binary.buildType.name != 'release') {
            classifierBase += binary.buildType.name
        }
        return classifierBase
    }

    /**
     * Gets the base classifier for a binary. This does not include the static or build type flags
     */
    @CompileStatic
    public static String getBaseClassifier(NativeLibraryBinarySpec binary) {
        def classifierBase = binary.targetPlatform.operatingSystem.name + binary.targetPlatform.architecture.name
        return classifierBase
    }

    /**
     * Gets the classifier for a dependency, matching the build type of the binary
     */
    public static String getDependencyClassifier(NativeBinarySpec binary, String depTypeClassifier) {
        def classifierBase = binary.targetPlatform.operatingSystem.name + binary.targetPlatform.architecture.name + depTypeClassifier
        if (binary.buildType.name != 'release') {
            classifierBase += binary.buildType.name
        }
        return classifierBase
    }


    /**
     * Gets the artifact classifier for a specific binary to be published
     */
    @CompileStatic
    public static String getPublishClassifier(NativeLibraryBinarySpec binary) {
        def classifierBase = binary.targetPlatform.operatingSystem.name + binary.targetPlatform.architecture.name
        if (binary instanceof StaticLibraryBinarySpec) {
            classifierBase += 'static'
        }
        if (binary.buildType.name != 'release') {
            classifierBase += binary.buildType.name
        }
        return classifierBase
    }

    /**
     * Sets an include flag in the compiler that is platform specific
     */
    @CompileStatic
    public static String setPlatformSpecificIncludeFlag(String loc, Tool cppCompiler) {
        if (OperatingSystem.current().isWindows()) {
            cppCompiler.args "/I$loc"
        } else {
            cppCompiler.args '-I', loc
        }
    }

    @Override
    void apply(Project project) {

        project.gradle.buildFinished {
            toolChainPathCache.clear()
            enabledConfigCache.clear()
            BuildConfigRulesBase.finalizeBuild()
        }

        project.plugins.withType(NativeComponentPlugin) {
            project.ext.BuildConfig = edu.wpi.first.nativeutils.configs.BuildConfig;
            project.ext.CrossBuildConfig = edu.wpi.first.nativeutils.configs.CrossBuildConfig;
            project.ext.NativeBuildConfig = edu.wpi.first.nativeutils.configs.NativeBuildConfig;
            project.ext.ToolchainPluginBuildConfig = edu.wpi.first.nativeutils.configs.ToolchainPluginBuildConfig

            project.pluginManager.apply(edu.wpi.first.nativeutils.rules.BuildConfigRules)

            project.ext.DependencyConfig = edu.wpi.first.nativeutils.configs.DependencyConfig

            project.pluginManager.apply(edu.wpi.first.nativeutils.rules.DependencyConfigRules)

            project.ext.NativeUtils = edu.wpi.first.nativeutils.NativeUtils

            project.ext.ExportsConfig = edu.wpi.first.nativeutils.configs.ExportsConfig

            project.ext.ExportsGenerationTask = edu.wpi.first.nativeutils.tasks.ExportsGenerationTask

            project.ext.WPINativeDependencySet = edu.wpi.first.nativeutils.dependencysets.WPINativeDependencySet

            project.pluginManager.apply(edu.wpi.first.nativeutils.rules.ExportsConfigRules)
        }


    }
}
