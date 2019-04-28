package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeLibraryBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.nativeutils.configs.DependencyConfig;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultDependencyConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultExportsConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultPlatformConfig;
import edu.wpi.first.toolchain.ToolchainExtension;
import jaci.gradle.nativedeps.DelegatedDependencySet;
import jaci.gradle.nativedeps.DependencySpecExtension;

public class NativeUtilsExtension {

  private final NamedDomainObjectContainer<PlatformConfig> platformConfigs;

  private final NamedDomainObjectContainer<ExportsConfig> exportsConfigs;

  private final NamedDomainObjectContainer<DependencyConfig> dependencyConfigs;

  private final Project project;

  private DependencySpecExtension dse = null;

  private List<String> platformsToConfigure = new ArrayList<>();

  @Inject
  public NativeUtilsExtension(Project project, ToolchainExtension tcExt) {
    this.project = project;

    exportsConfigs = project.container(ExportsConfig.class, name -> {
      return project.getObjects().newInstance(DefaultExportsConfig.class, name);
    });

    dependencyConfigs = project.container(DependencyConfig.class, name -> {
      return project.getObjects().newInstance(DefaultDependencyConfig.class, name);
    });

    platformConfigs = project.container(PlatformConfig.class, name -> {
      return project.getObjects().newInstance(DefaultPlatformConfig.class, name);
    });

    project.afterEvaluate(proj -> {
      for (PlatformConfig config : platformConfigs) {
        if (config.getPlatformPath() == null) {
          throw new GradleException("Platform Path cannot be null: " + config.getName());
        }
      }
    });

  }

  public NamedDomainObjectContainer<PlatformConfig> getPlatformConfigs() {
    return platformConfigs;
  }

  void platformConfigs(final Action<? super NamedDomainObjectContainer<PlatformConfig>> closure) {
    closure.execute(platformConfigs);
  }

  public NamedDomainObjectContainer<ExportsConfig> getExportsConfigs() {
    return exportsConfigs;
  }

  void exportsConfigs(final Action<? super NamedDomainObjectContainer<ExportsConfig>> closure) {
    closure.execute(exportsConfigs);
  }

  public NamedDomainObjectContainer<DependencyConfig> getDependencyConfigs() {
    return dependencyConfigs;
  }

  void dependencyConfigs(final Action<? super NamedDomainObjectContainer<DependencyConfig>> closure) {
    closure.execute(dependencyConfigs);
  }

  public String getPlatformPath(NativeBinarySpec binary) {
    PlatformConfig platform = platformConfigs.findByName(binary.getTargetPlatform().getName());
    if (platform == null ) {
      return binary.getTargetPlatform().getOperatingSystem().getName() + "/" + binary.getTargetPlatform().getArchitecture().getName();
    }
    return platform.getPlatformPath();
  }

  public String getDependencyClassifier(NativeBinarySpec binary, String depTypeClassifier) {
    String classifierBase = binary.getTargetPlatform().getName();
    if (!binary.getBuildType().getName().equals("release")) {
      classifierBase += binary.getBuildType().getName();
    }
    return classifierBase;
  }

  public String getPublishClassifier(NativeLibraryBinarySpec binary) {
    String classifierBase = binary.getTargetPlatform().getName();
    if (binary instanceof StaticLibraryBinarySpec) {
        classifierBase += "static";
    }
    if (!binary.getBuildType().getName().contains("release")) {
        classifierBase += binary.getBuildType().getName();
    }
    return classifierBase;
  }

  public void useLibrary(VariantComponentSpec component, boolean skipOnUnknown, String... libraries) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      useLibrary((NativeBinarySpec)binary, skipOnUnknown, libraries);
    });
  }

  public void useLibrary(NativeBinarySpec binary, boolean skipOnUnknown, String... libraries) {
    if (dse == null) {
      dse = project.getExtensions().getByType(DependencySpecExtension.class);
    }
    for (String library : libraries) {
      binary.lib(new DelegatedDependencySet(library, binary, dse, skipOnUnknown));
    }
  }

  public void useAllPlatforms(PlatformAwareComponentSpec component) {
    for (String platform : platformsToConfigure) {
      component.targetPlatform(platform);
    }
  }

  public void addPlatformToConfigure(String platform) {
    platformsToConfigure.add(platform);
  }

  public void configurePlatform(String name, Action<? super PlatformConfig> action) {
    getPlatformConfigs().getByName(name, action);
  }
}
