package edu.wpi.first.nativeutils.rules

import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.model.*
import org.gradle.platform.base.BinaryContainer
import edu.wpi.first.nativeutils.dependencysets.*
import edu.wpi.first.nativeutils.NativeUtils
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask
import edu.wpi.first.nativeutils.tasks.NativeDependencyDownload
import edu.wpi.first.nativeutils.tasks.NativeDependencyCombiner
import edu.wpi.first.nativeutils.configs.DependencyConfig
import groovy.transform.CompileStatic
import org.gradle.nativeplatform.NativeBinarySpec
import org.gradle.nativeplatform.NativeComponentSpec
import org.gradle.api.tasks.Exec
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

    private void addDependency(Configuration configuration, java.util.LinkedHashMap<java.lang.String, java.lang.String> map) {
        configuration map
    }

    @Mutate
    @CompileStatic
    void setupDependencyDownloads(ModelMap<Task> tasks, DependencyConfigSpec configs, BinaryContainer binaries,
                                  ProjectLayout projectLayout, BuildConfigSpec buildConfigs) {
        def currentProject = (Project) projectLayout.projectIdentifier
        def rootProject = (Project) currentProject.rootProject

        def headerClassifiers = []

        def configurationList = []

        def sortedConfigs = configs.toSorted { a, b -> a.sortOrder <=> b.sortOrder }

        for (DependencyConfig config : sortedConfigs) {
            headerClassifiers.add(config.headerClassifier)
            currentProject.dependencies {
                def dep = (DependencyHandler) it
                def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: config.headerClassifier, ext: config.ext]
                def configurationName = "${config.groupId}${config.artifactId}${config.headerClassifier}".toString()
                configurationName = configurationName.replace('.', '')
                configurationList << new Tuple(config.artifactId, config.headerClassifier, currentProject.configurations.create(configurationName))
                dep.add(configurationName, map)
            }
            for (BuildConfig buildConfig : buildConfigs) {
                if (!(BuildConfigRulesBase.isConfigEnabled(buildConfig, currentProject))) {
                    continue
                }
                currentProject.dependencies {
                    def dep = (DependencyHandler) it
                    def classifier = NativeUtils.getClassifier((BuildConfig) buildConfig)
                    def map = [group: config.groupId, name: config.artifactId, version: config.version, classifier: classifier, ext: config.ext]
                    def configurationName = "${config.groupId}${config.artifactId}${classifier}".toString()
                    configurationName = configurationName.replace('.', '')
                    configurationList << new Tuple(config.artifactId, classifier, currentProject.configurations.create(configurationName))
                    dep.add(configurationName, map)
                }
            }
        }

        def depLocation = "${rootProject.buildDir}/dependencies"

        def downloadAllTaskName = 'downloadAllDependencies'
        def downloadAllTask = rootProject.tasks.findByPath(downloadAllTaskName)
        if (downloadAllTask == null) {
            downloadAllTask = rootProject.tasks.create(downloadAllTaskName, NativeDependencyCombiner) {
                NativeDependencyCombiner combineTask = (NativeDependencyCombiner) it;
                combineTask.group = 'Dependencies'
                combineTask.description = 'Downloads and extracts all native c++ dependencies'
            }
        }

        for (Tuple t : configurationList) {
            String id = (String) t.get(0)
            String classifier = (String) t.get(1)
            Configuration configuration = (Configuration) t.get(2)
            def taskName = "download${configuration.name}"
            def task = rootProject.tasks.findByPath(taskName)
            if (task == null) {
                task = rootProject.tasks.create(taskName, NativeDependencyDownload) {
                    def createdTask = (NativeDependencyDownload) it
                    createdTask.group = 'Dependencies'
                    createdTask.description = 'Downloads and extracts a native c++ dependency'
                    createdTask.dependsOn configuration

                    createdTask.from {
                        configuration.collect {
                            currentProject.zipTree(it)
                        }
                    }

                    createdTask.into "$depLocation/${id.toLowerCase()}/${classifier}"

                }
                downloadAllTask.dependsOn task
            }
            binaries.findAll { BuildConfigRulesBase.isNativeProject((BinarySpec) it) }.each { oBinary ->
                NativeBinarySpec binary = (NativeBinarySpec) oBinary
                if (NativeUtils.getClassifier(binary) == classifier || headerClassifiers.contains(classifier)) {
                    binary.tasks.withType(AbstractNativeSourceCompileTask) { compTask ->
                        ((Task) compTask).dependsOn task
                    }
                }
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

        def depLocation = "${rootProject.buildDir}/dependencies"

        sortedConfigs.each { config ->
            def nativeBinaries = binaries.findAll { BuildConfigRulesBase.isNativeProject((BinarySpec) it) }
            nativeBinaries.each { oBinary ->
                def binary = (NativeBinarySpec) oBinary
                def component = binary.component
                if (config.sharedConfigs != null && config.sharedConfigs.containsKey(component.name)) {
                    if (config.sharedConfigs.get(component.name).size() == 0 ||
                            config.sharedConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new SharedDependencySet("$depLocation/${config.artifactId.toLowerCase()}", binary, config.artifactId, currentProject))
                    }
                }

                if (config.staticConfigs != null && config.staticConfigs.containsKey(component.name)) {
                    if (config.staticConfigs.get(component.name).size() == 0 ||
                            config.staticConfigs.get(component.name).contains("${binary.targetPlatform.operatingSystem.name}:${binary.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new StaticDependencySet("$depLocation/${config.artifactId.toLowerCase()}", binary, config.artifactId, currentProject))
                    }
                }
            }
        }
    }
}
