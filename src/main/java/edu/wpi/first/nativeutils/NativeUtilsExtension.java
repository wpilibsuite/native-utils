package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import edu.wpi.first.nativeutils.configs.CrossCompilerConfig;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultCrossCompilerConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultPlatformConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultExportsConfig;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainRegistrar;

public class NativeUtilsExtension {
  private final NamedDomainObjectContainer<CrossCompilerConfig> configurableCrossCompilers;
  private final List<NamedDomainObjectContainer<CrossCompilerConfig>> configurableCrossCompilersList = new ArrayList<>();

  private final NamedDomainObjectContainer<PlatformConfig> platformConfigs;
  private final List<NamedDomainObjectContainer<PlatformConfig>> platformConfigsList = new ArrayList<>();

  private final NamedDomainObjectContainer<ExportsConfig> exportsConfigs;
  private final List<NamedDomainObjectContainer<ExportsConfig>> exportsConfigList = new ArrayList<>();
  private final Project project;

  @Inject
  public NativeUtilsExtension(Project project, ToolchainExtension tcExt) {
    this.project = project;
    configurableCrossCompilers = project.container(CrossCompilerConfig.class, name -> {
      return project.getObjects().newInstance(DefaultCrossCompilerConfig.class, name);
    });

    exportsConfigs = project.container(ExportsConfig.class, name -> {
      return project.getObjects().newInstance(DefaultExportsConfig.class, name);
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
}
