package edu.wpi.first.nativeutils.rules

import org.apache.tools.ant.taskdefs.email.Header
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.model.*
import org.gradle.platform.base.BinaryContainer
import edu.wpi.first.nativeutils.dependencysets.*
import edu.wpi.first.nativeutils.NativeUtils
import org.gradle.api.file.FileTree
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import edu.wpi.first.nativeutils.configs.DependencyConfig
import groovy.transform.CompileStatic
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeComponentSpec
import org.gradle.api.tasks.Exec

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import edu.wpi.first.nativeutils.configs.BuildConfig
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.platform.base.BinarySpec

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class DependencyConfigRules extends RuleSource {

    @Validate
    @CompileStatic
    void validateAllConfigsHaveProperties(DependencyConfigSpec configs) {
        for (DependencyConfig config : configs) {
            assert config.groupId != null && config.groupId != ''
            assert config.artifactId != null && config.artifactId != ''
            assert config.headerClassifier != null && config.headerClassifier != ''
            assert config.ext != null && config.ext != ''
            assert config.version != null && config.version != ''
        }
    }

    @Validate
    @CompileStatic
    void assertDependenciesAreNotNullMaps(DependencyConfigSpec configs) {
        for (DependencyConfig config : configs) {
            if (config.sharedConfigs != null) {
                config.sharedConfigs.each {
                    assert it.value != null
                }
            }
            if (config.staticConfigs != null) {
                config.staticConfigs.each {
                    assert it.value != null
                }
            }
        }
    }

    @Validate
    @CompileStatic
    void vaidateDependenciesDontSpecifyAll(DependencyConfigSpec configs) {
        for (DependencyConfig config : configs) {
            def sharedConfigs
            if (config.sharedConfigs != null) {
                sharedConfigs = config.sharedConfigs.collect { it.key }
            } else {
                sharedConfigs = []
            }

            def staticConfigs
            if (config.staticConfigs != null) {
                staticConfigs = config.staticConfigs.collect { it.key }
            } else {
                staticConfigs = []
            }

            sharedConfigs.intersect(staticConfigs).each { common ->
                assert config.sharedConfigs.get(common).size() != 0 && config.staticConfigs.get(common).size() != 0
            }
        }
    }

    @Validate
    @CompileStatic
    void validateDependenciesDontIntersectSharedStatic(DependencyConfigSpec configs) {
        for (DependencyConfig config : configs) {
            def sharedConfigs
            if (config.sharedConfigs != null) {
                sharedConfigs = config.sharedConfigs.collect { it.key }
            } else {
                sharedConfigs = []
            }

            def staticConfigs
            if (config.staticConfigs != null) {
                staticConfigs = config.staticConfigs.collect { it.key }
            } else {
                staticConfigs = []
            }

            sharedConfigs.intersect(staticConfigs).each { common ->
                def sharedDeps = config.sharedConfigs.get(common)
                def staticDeps = config.staticConfigs.get(common)
                assert staticDeps.intersect(sharedDeps).size() == 0
            }
        }
    }

    private String createDependency(Project rootProject, DependencyConfig config, String classifier) {
        def configurationName = "${config.groupId}${config.artifactId}${classifier}".toString()
        configurationName = configurationName.replace('.', '')
        rootProject.dependencies {
            def dep = (DependencyHandler) it
            def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: classifier, ext: config.ext]
            try {
                rootProject.configurations.maybeCreate(configurationName)
                dep.add(configurationName, map)
            } catch (InvalidUserDataException) {
            }
        }
        return configurationName
    }

    @CompileStatic
    private void addDependenciesToBinary(Project rootProject, DependencyConfig config, NativeBinarySpec binary) {
        def component = binary.component

        // Headers and sources have already been created

        def headerConfigurationName = "${config.groupId}${config.artifactId}${config.headerClassifier}".toString()
        headerConfigurationName = headerConfigurationName.replace('.', '')

        def sourceConfigurationName = config.sourceClassifier
        if (sourceConfigurationName != null) {
            sourceConfigurationName = "${config.groupId}${config.artifactId}${sourceConfigurationName}".toString()
            sourceConfigurationName = sourceConfigurationName.replace('.', '')
        }

        List<String> linkExcludes = config.linkExcludes

        if (linkExcludes == null) {
            linkExcludes = []
        }

        if (config.sharedConfigs != null && config.sharedConfigs.containsKey(component.name)) {
            if (config.sharedConfigs.get(component.name).size() == 0 ||
                    config.sharedConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                // Setup a shared dependency
                String libConfigurationName = createDependency(rootProject, config, NativeUtils.getDependencyClassifier(binary, ''))
                if (config.compileOnlyShared) {
                    binary.lib(new SharedCompileOnlyDependencySet(binary, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes))
                } else {
                    binary.lib(new SharedDependencySet(binary, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes))
                }
                return
            }
        }
        if (config.staticConfigs != null && config.staticConfigs.containsKey(component.name)) {
            if (config.staticConfigs.get(component.name).size() == 0 ||
                    config.staticConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                // Setup a static dependency
                String libConfigurationName = createDependency(rootProject, config, NativeUtils.getDependencyClassifier(binary, 'static'))
                binary.lib(new StaticDependencySet(binary, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes))
                return
            }
        }
        if (config.headerOnlyConfigs != null && config.headerOnlyConfigs.containsKey(component.name)) {
            if (config.headerOnlyConfigs.get(component.name).size() == 0 ||
                    config.headerOnlyConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                binary.lib(new HeaderOnlyDependencySet(binary, headerConfigurationName, rootProject))
            }
        }
    }

    @Validate
    @CompileStatic
    void setupDependencies(BinaryContainer binaries, DependencyConfigSpec configs,
                           ProjectLayout projectLayout, BuildConfigSpec buildConfigs) {
        def currentProject = (Project) projectLayout.projectIdentifier
        def rootProject = (Project) currentProject.rootProject

        def sortedConfigs = configs.toSorted { a, b -> a.sortOrder <=> b.sortOrder }

        for (DependencyConfig config : sortedConfigs) {
            rootProject.dependencies {
                def dep = (DependencyHandler) it
                def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: config.headerClassifier, ext: config.ext]
                def configurationName = "${config.groupId}${config.artifactId}${config.headerClassifier}".toString()
                configurationName = configurationName.replace('.', '')
                try {
                    rootProject.configurations.create(configurationName)
                    dep.add(configurationName, map)
                } catch (InvalidUserDataException) {
                }
            }

            if (config.sourceClassifier != null) {
                rootProject.dependencies {
                    def dep = (DependencyHandler) it
                    def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: config.sourceClassifier, ext: config.ext]
                    def configurationName = "${config.groupId}${config.artifactId}${config.sourceClassifier}".toString()
                    configurationName = configurationName.replace('.', '')
                    try {
                        rootProject.configurations.create(configurationName)
                        dep.add(configurationName, map)
                    } catch (InvalidUserDataException) {
                    }
                }
            }

            def nativeBinaries = binaries.findAll { BuildConfigRulesBase.isNativeProject((BinarySpec) it) }
            for (Object oBinary : nativeBinaries) {
                def binary = (NativeBinarySpec) oBinary
                if (!binary.buildable) {
                    continue
                }
                addDependenciesToBinary(rootProject, config, binary)

            }
        }
    }
}
