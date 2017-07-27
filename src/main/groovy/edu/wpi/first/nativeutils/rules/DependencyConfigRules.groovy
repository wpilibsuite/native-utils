package edu.wpi.first.nativeutils.rules

import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.language.base.internal.ProjectLayout
import org.gradle.model.*
import org.gradle.platform.base.BinaryContainer
import edu.wpi.first.nativeutils.dependencysets.*
import edu.wpi.first.nativeutils.NativeUtils

@SuppressWarnings("GroovyUnusedDeclaration")
class DependencyConfigRules extends RuleSource {

    @Validate
    void validateAllConfigsHaveProperties(DependencyConfigSpec configs) {
        configs.each { config->
            assert config.groupId != null && config.groupId != ''
            assert config.artifactId != null && config.artifactId != ''
            assert config.headerClassifier != null && config.headerClassifier != ''
            assert config.ext != null && config.ext != ''
            assert config.version != null && config.version != ''
        }
    }

    @Validate
    void assertDependenciesAreNotNullMaps(DependencyConfigSpec configs) {
        configs.each { config->
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
    void vaidateDependenciesDontSpecifyAll(DependencyConfigSpec configs) {
        configs.each { config->
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

            sharedConfigs.intersect(staticConfigs).each { common->
                assert config.sharedConfigs.get(common).size() != 0 && config.staticConfigs.get(common).size() != 0
            }
        }
    }

    @Validate
    void validateDependenciesDontIntersectSharedStatic(DependencyConfigSpec configs) {
        configs.each { config->
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

            sharedConfigs.intersect(staticConfigs).each { common->
                def sharedDeps = config.sharedConfigs.get(common)
                def staticDeps = config.staticConfigs.get(common)
                assert staticDeps.intersect(sharedDeps).size() == 0
            }
        }
    }

    @Mutate
    void setupDependencyDownloads(ModelMap<Task> tasks, DependencyConfigSpec configs, BinaryContainer binaries,
                        ProjectLayout projectLayout, BuildConfigSpec buildConfigs) {
        def rootProject = projectLayout.projectIdentifier.rootProject
        def currentProject = projectLayout.projectIdentifier

        currentProject.configurations.create('nativeDeps')

        def headerClassifiers = []

        def sortedConfigs = configs.toSorted { a, b -> a.sortOrder<=>b.sortOrder }

        sortedConfigs.each { config->
            headerClassifiers.add(config.headerClassifier)
            currentProject.dependencies {
                nativeDeps group: config.groupId, name: config.artifactId, version: config.version, classifier: config.headerClassifier, ext: config.ext
            }
            buildConfigs.findAll { BuildConfigRulesBase.isConfigEnabled(it, projectLayout.projectIdentifier) }.each { buildConfig ->
                currentProject.dependencies {
                    nativeDeps group: config.groupId, name: config.artifactId, version: config.version, classifier: NativeUtils.getClassifier(buildConfig), ext: config.ext
                }
            }
        }

        def depLocation = "${rootProject.buildDir}/dependencies"

        def filesList = currentProject.configurations.nativeDeps.files

        currentProject.configurations.nativeDeps.dependencies.each { dependency ->
            def classifier = dependency.artifacts[0].classifier
            def extension = dependency.artifacts[0].extension
            def taskName = "download${dependency.group}${dependency.name}${classifier}"
            def task = rootProject.tasks.findByPath(taskName)
            if (task == null) {
                task = rootProject.tasks.create(taskName, Copy) {
                    def file
                    filesList.each {
                        if (it.toString().endsWith("${classifier}.${extension}") && it.toString().contains("${dependency.name}-".toString())) {
                            file = it
                        }
                    }
                    from rootProject.zipTree(file)
                    into "$depLocation/${dependency.name.toLowerCase()}/${classifier}"
                }
                binaries.findAll { BuildConfigRulesBase.isNativeProject(it) }.each { binary ->
                    if (NativeUtils.getClassifier(binary) == classifier || headerClassifiers.contains(classifier)) {
                        binary.buildTask.dependsOn task
                    }
                }
            }
        }
    }

    @Validate
    void setupDependencies(BinaryContainer binaries, DependencyConfigSpec configs, 
                        ProjectLayout projectLayout, BuildConfigSpec buildConfigs) {
        def rootProject = projectLayout.projectIdentifier.rootProject
        def currentProject = projectLayout.projectIdentifier

        def sortedConfigs = configs.toSorted { a, b -> a.sortOrder<=>b.sortOrder }

        def depLocation = "${rootProject.buildDir}/dependencies"

        sortedConfigs.each { config ->
            def nativeBinaries = binaries.findAll { BuildConfigRulesBase.isNativeProject(it) }
            nativeBinaries.each { binary ->
                def component = binary.component
                if (config.sharedConfigs != null && config.sharedConfigs.containsKey(component.name)) {
                    if (config.sharedConfigs.get(component.name).size() == 0 ||
                        config.sharedConfigs.get(component.name).contains("${it.targetPlatform.operatingSystem.name}:${it.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new SharedDependencySet("$depLocation/${config.artifactId.toLowerCase()}", binary, config.artifactId, currentProject))
                    }
                }

                if (config.staticConfigs != null && config.staticConfigs.containsKey(component.name)) {
                    if (config.staticConfigs.get(component.name).size() == 0 ||
                        config.staticConfigs.get(component.name).contains("${it.targetPlatform.operatingSystem.name}:${it.targetPlatform.architecture.name}".toString())) {
                        binary.lib(new StaticDependencySet("$depLocation/${config.artifactId.toLowerCase()}", binary, config.artifactId, currentProject))
                    }
                }
            }
        }
    }
}