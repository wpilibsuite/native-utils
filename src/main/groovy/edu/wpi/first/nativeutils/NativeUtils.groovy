package edu.wpi.first.nativeutils

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.internal.project.ProjectIdentifier
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.Tool
import edu.wpi.first.nativeutils.configs.BuildConfig
import edu.wpi.first.nativeutils.rules.BuildConfigRulesBase
import edu.wpi.first.nativeutils.configs.CrossBuildConfig

/**
 * Created by 333fr on 3/1/2017.
 */
public class NativeUtils implements Plugin<Project> {

    static File extractedFile = null

    static String getGeneratorFilePath() {
        if (extractedFile != null) {
            return extractedFile.toString()
        }

        InputStream is = NativeUtils.class.getResourceAsStream("/DefFileGenerator.exe");
        extractedFile = File.createTempFile("DefFileGenerator", ".exe")
        extractedFile.deleteOnExit();

        OutputStream os = new FileOutputStream(extractedFile);

        byte[] buffer = new byte[1024];
        int readBytes;
        try {
            while ((readBytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, readBytes);
            }
        } finally {
            os.close();
            is.close();
        }

        return extractedFile.toString()
    }

    private static final Map<CrossBuildConfig, Boolean> enabledConfigCache = [:]

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

    /**
     * Gets the toolChainPath for the specific build configuration
     */
    public static String getToolChainPath(BuildConfig config, Project project) {
        if (!BuildConfigRulesBase.isCrossCompile(config)) {
            return null
        }

        if (toolChainPathCache.containsKey(config)) {
            return toolChainPathCache.get(config)
        }
        // Try getting the toolChainPath
        for (item in project.properties) {
            def key = item.key
            def value = item.value
            if (key.contains('-toolChainPath')) {
                String[] configSplit = key.split("-", 2);
                if (value != null && configSplit.length == 2 && configSplit[0] != "") {
                    if (configSplit[0] == config.architecture) {
                        toolChainPathCache.put(config, value)
                        return value
                    }
                }
            }
        }

        toolChainPathCache.put(config, config.toolChainPath)
        return config.toolChainPath
    }

    public static boolean isConfigEnabled(BuildConfig config, ProjectIdentifier identifier) {
        return BuildConfigRulesBase.isConfigEnabled(config, identifier)
    }

    /**
     * Gets the extraction platform path for the specific build configuration
     */
    public static String getPlatformPath(BuildConfig config) {
        return config.operatingSystem + '/' + config.architecture
    }

    /**
     * Gets the artifact classifier for a specifc build configuration
     */
    public static String getClassifier(BuildConfig config) {
        return config.operatingSystem + config.architecture
    }

    /**
     * Gets the extraction platform path for a specific binary
     */
    public static String getPlatformPath(NativeBinarySpec binary) {
        return binary.targetPlatform.operatingSystem.name + '/' + binary.targetPlatform.architecture.name
    }

    /**
     * Gets the artifact classifier for a specific binary
     */
    public static String getClassifier(NativeBinarySpec binary) {
        return binary.targetPlatform.operatingSystem.name + binary.targetPlatform.architecture.name
    }

    /**
     * Sets an include flag in the compiler that is platform specific
     */
    public static String setPlatformSpecificIncludeFlag(String loc, Tool cppCompiler) {
        if (OperatingSystem.current().isWindows()) {
            cppCompiler.args "/I$loc"
        } else {
            cppCompiler.args '-I', loc
        }
    }

    @Override
    void apply(Project project) {
        project.ext.BuildConfig = edu.wpi.first.nativeutils.configs.BuildConfig
        project.ext.CrossBuildConfig = edu.wpi.first.nativeutils.configs.CrossBuildConfig

        project.pluginManager.apply(edu.wpi.first.nativeutils.rules.BuildConfigRules)

        project.ext.DependencyConfig = edu.wpi.first.nativeutils.configs.DependencyConfig

        project.pluginManager.apply(edu.wpi.first.nativeutils.rules.DependencyConfigRules)

        project.ext.JNIConfig = edu.wpi.first.nativeutils.configs.JNIConfig

        project.pluginManager.apply(edu.wpi.first.nativeutils.rules.JNIConfigRules)

        project.ext.ExportsConfig = edu.wpi.first.nativeutils.configs.ExportsConfig

        project.pluginManager.apply(edu.wpi.first.nativeutils.rules.ExportsConfigRules)
    }
}
