package edu.wpi.first.nativeutils.rules

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
            for (BuildConfig buildConfig : buildConfigs) {
                if (!(BuildConfigRulesBase.isConfigEnabled(buildConfig, currentProject))) {
                    continue
                }
                rootProject.dependencies {
                    def dep = (DependencyHandler) it
                    def classifier = NativeUtils.getClassifier((BuildConfig) buildConfig)
                    def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: classifier, ext: config.ext]
                    def configurationName = "${config.groupId}${config.artifactId}${classifier}".toString()
                    configurationName = configurationName.replace('.', '')
                    try {
                        rootProject.configurations.create(configurationName)
                        dep.add(configurationName, map)
                    } catch (InvalidUserDataException) {
                    }
                }
            }

            def nativeBinaries = binaries.findAll { BuildConfigRulesBase.isNativeProject((BinarySpec) it) }
            nativeBinaries.each { oBinary ->
                def binary = (NativeBinarySpec) oBinary
                def component = binary.component

                def headerConfigurationName = "${config.groupId}${config.artifactId}${config.headerClassifier}".toString()
                headerConfigurationName = headerConfigurationName.replace('.', '')
                def headerConfig = rootProject.configurations.getByName(headerConfigurationName)
                FileTree headerZip = rootProject.zipTree(headerConfig.dependencies.collectMany { headerConfig.files(it) as Collection }.first())

                def sourceClassifier = config.sourceClassifier
                FileTree sourceZip = rootProject.files().asFileTree
                if (sourceClassifier != null) {
                    def sourceConfigurationName = "${config.groupId}${config.artifactId}${sourceClassifier}".toString()
                    sourceConfigurationName = sourceConfigurationName.replace('.', '')
                    def sourceConfig = rootProject.configurations.getByName(sourceConfigurationName)
                    sourceZip = rootProject.zipTree(sourceConfig.dependencies.collectMany { sourceConfig.files(it) as Collection }.first())
                }

                def libConfigurationName = "${config.groupId}${config.artifactId}${NativeUtils.getClassifier(binary)}".toString()
                libConfigurationName = libConfigurationName.replace('.', '')
                def libConfig = rootProject.configurations.getByName(libConfigurationName)
                def libZip = rootProject.zipTree(libConfig.dependencies.collectMany { libConfig.files(it) as Collection }.first())

                if (config.sharedConfigs != null && config.sharedConfigs.containsKey(component.name)) {
                    if (config.sharedConfigs.get(component.name).size() == 0 ||
                            config.sharedConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new SharedDependencySet(binary, headerZip, libZip, sourceZip, rootProject))
                    }
                }

                if (config.staticConfigs != null && config.staticConfigs.containsKey(component.name)) {
                    if (config.staticConfigs.get(component.name).size() == 0 ||
                            config.staticConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new StaticDependencySet(binary, headerZip, libZip, sourceZip, rootProject))
                    }
                }
            }
        }
    }
}
