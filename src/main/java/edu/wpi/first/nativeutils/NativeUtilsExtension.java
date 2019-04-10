package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeComponentSpec;
import org.gradle.nativeplatform.NativeLibraryBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.nativeutils.configs.CrossCompilerConfig;
import edu.wpi.first.nativeutils.configs.DependencyConfig;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultCrossCompilerConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultDependencyConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultExportsConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultPlatformConfig;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainRegistrar;
import jaci.gradle.nativedeps.DelegatedDependencySet;
import jaci.gradle.nativedeps.DependencySpecExtension;

public class NativeUtilsExtension {
  private final NamedDomainObjectContainer<CrossCompilerConfig> configurableCrossCompilers;
  private final List<NamedDomainObjectContainer<CrossCompilerConfig>> configurableCrossCompilersList = new ArrayList<>();

  private final NamedDomainObjectContainer<PlatformConfig> platformConfigs;
  private final List<NamedDomainObjectContainer<PlatformConfig>> platformConfigsList = new ArrayList<>();

  private final NamedDomainObjectContainer<ExportsConfig> exportsConfigs;
  private final List<NamedDomainObjectContainer<ExportsConfig>> exportsConfigList = new ArrayList<>();


  private final NamedDomainObjectContainer<DependencyConfig> dependencyConfigs;
  private final List<NamedDomainObjectContainer<DependencyConfig>> dependencyConfigsList = new ArrayList<>();

  private final Project project;

  private DependencySpecExtension dse = null;

  private List<String> platformsToConfigure = new ArrayList<>();

  @Inject
  public NativeUtilsExtension(Project project, ToolchainExtension tcExt) {
    this.project = project;
    configurableCrossCompilers = project.container(CrossCompilerConfig.class, name -> {
      return project.getObjects().newInstance(DefaultCrossCompilerConfig.class, name);
    });

    exportsConfigs = project.container(ExportsConfig.class, name -> {
      return project.getObjects().newInstance(DefaultExportsConfig.class, name);
    });

    dependencyConfigs = project.container(DependencyConfig.class, name -> {
      return project.getObjects().newInstance(DefaultDependencyConfig.class, name);
    });

    configurableCrossCompilers.all(config -> {
      ToolchainDescriptor descriptor = new ToolchainDescriptor(config.getName(), config.getName() + "ConfiguredGcc", new ToolchainRegistrar<ConfigurableGcc>(ConfigurableGcc.class, project));

      tcExt.add(descriptor);

      project.afterEvaluate(proj -> {
        descriptor.setToolchainPlatforms(config.getOperatingSystem() + config.getArchitecture());
        descriptor.setOptional(config.getOptional());
        descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, name -> {
          String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
          return config.getCompilerPrefix() + name + exeSuffix;
        }));
      });
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

    configurableCrossCompilersList.add(configurableCrossCompilers);
    platformConfigsList.add(platformConfigs);
    exportsConfigList.add(exportsConfigs);
    dependencyConfigsList.add(dependencyConfigs);
  }

  public NamedDomainObjectContainer<CrossCompilerConfig> getConfigurableCrossCompilers() {
    return configurableCrossCompilers;
  }

  void configurableCrossCompilers(final Action<NamedDomainObjectContainer<CrossCompilerConfig>> closure) {
    project.configure(configurableCrossCompilersList, closure);
  }

  public NamedDomainObjectContainer<PlatformConfig> getPlatformConfigs() {
    return platformConfigs;
  }

  void platformConfigs(final Action<NamedDomainObjectContainer<PlatformConfig>> closure) {
    project.configure(platformConfigsList, closure);
  }

  public NamedDomainObjectContainer<ExportsConfig> getExportsConfigs() {
    return exportsConfigs;
  }

  void exportsConfigs(final Action<NamedDomainObjectContainer<ExportsConfig>> closure) {
    project.configure(exportsConfigList, closure);
  }

  public NamedDomainObjectContainer<DependencyConfig> getDependencyConfigs() {
    return dependencyConfigs;
  }

  void dependencyConfigs(final Action<NamedDomainObjectContainer<DependencyConfig>> closure) {
    project.configure(dependencyConfigsList, closure);
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
}
