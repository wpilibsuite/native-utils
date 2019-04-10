package edu.wpi.first.nativeutils.rules;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeComponentSpec;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.DependencyConfig;
import edu.wpi.first.nativeutils.dependencysets.SharedCompileOnlyDependencySet;
import edu.wpi.first.nativeutils.dependencysets.SharedDependencySet;
import edu.wpi.first.nativeutils.dependencysets.StaticDependencySet;
import edu.wpi.first.nativeutils.dependencysets.HeaderOnlyDependencySet;

public class DependencyConfigRules extends RuleSource {

  private String createDependency(Project rootProject, DependencyConfig config, String classifier) {
    String configurationName = config.getGroupId() + config.getArtifactId() + classifier;// "${config.groupId}${config.artifactId}${classifier}".toString()
    configurationName = configurationName.replace(".", "");
    Map<String, String> depMap = new HashMap<>();
    depMap.put("group", config.getGroupId());
    depMap.put("name", config.getArtifactId());
    depMap.put("version", config.getVersion());
    depMap.put("classifier", classifier);
    depMap.put("ext", config.getExt());
    rootProject.getConfigurations().maybeCreate(configurationName);
    rootProject.getDependencies().add(configurationName, depMap);
    return configurationName;
  }

  private void addDependenciesToBinary(Project rootProject, NativeUtilsExtension extension, DependencyConfig config, NativeBinarySpec binary) {
    String componentName = binary.getComponent().getName();

    // Headers and sources have already been created

    String headerConfigurationName = config.getGroupId() + config.getArtifactId() + config.getHeaderClassifier();
    headerConfigurationName = headerConfigurationName.replace(".", "");

    String sourceConfigurationName = config.getSourceClassifier();
    if (sourceConfigurationName != null) {
      sourceConfigurationName = config.getGroupId() + config.getArtifactId() + sourceConfigurationName;
      sourceConfigurationName = sourceConfigurationName.replace(".", "");
    }

    List<String> linkExcludes = config.getLinkExcludes();

    List<String> sharedConfigs = config.getSharedConfigs().get(componentName);
    if (sharedConfigs != null) {
      if (sharedConfigs.isEmpty() || sharedConfigs.contains(binary.getTargetPlatform().getName())) {
        String libConfigurationName = createDependency(rootProject, config, extension.getDependencyClassifier(binary, ""));
        if (config.getCompileOnlyShared()) {
          binary.lib(new SharedCompileOnlyDependencySet(binary, extension, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes));
        } else {
          binary.lib(new SharedDependencySet(binary, extension, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes));
        }
        return;
      }
    }

    List<String> staticConfigs = config.getStaticConfigs().get(componentName);
    if (staticConfigs != null) {
      if (staticConfigs.isEmpty() || staticConfigs.contains(binary.getTargetPlatform().getName())) {
        String libConfigurationName = createDependency(rootProject, config, extension.getDependencyClassifier(binary, ""));
        binary.lib(new StaticDependencySet(binary, extension, headerConfigurationName, libConfigurationName, sourceConfigurationName, rootProject, linkExcludes));
        return;
      }
    }

    List<String> headerOnlyConfigs = config.getHeaderOnlyConfigs().get(componentName);
    if (headerOnlyConfigs != null) {
      if (headerOnlyConfigs.isEmpty() || headerOnlyConfigs.contains(binary.getTargetPlatform().getName())) {
        binary.lib(new HeaderOnlyDependencySet(binary, headerConfigurationName, rootProject));
        return;
      }
    }
  }

  @Validate
  public void setupDependencies(BinaryContainer binaries, ExtensionContainer extensions, ProjectLayout projectLayout) {
    Project currentProject = (Project)projectLayout.getProjectIdentifier();
    Project rootProject = currentProject.getRootProject();

    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    extension.getDependencyConfigs().stream().sorted(Comparator.comparing(DependencyConfig::getSortOrder)).forEach(config -> {
      createDependency(rootProject, config, config.getHeaderClassifier());

      if (config.getSourceClassifier() != null) {
        createDependency(rootProject, config, config.getSourceClassifier());
      }

      for (BinarySpec oBinary : binaries) {
        if (!(oBinary instanceof NativeBinarySpec)) continue;
        NativeBinarySpec binary = (NativeBinarySpec)oBinary;
        if (!binary.isBuildable()) continue;
        addDependenciesToBinary(rootProject, extension, config, binary);
      }
    });
  }
}
