package edu.wpi.first.nativeutils;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import edu.wpi.first.nativeutils.configs.ConfigurableCrossPlatformConfig;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultConfigurableCrossPlatformConfig;
import edu.wpi.first.nativeutils.rules.ExportsConfigRules;
import edu.wpi.first.nativeutils.rules.PlatformRules;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainPlugin;
import edu.wpi.first.toolchain.ToolchainRegistrar;

public class NativeUtils implements Plugin<Project> {
  @Override
  public void apply(Project project) {

    project.getPluginManager().apply(ToolchainPlugin.class);

    ToolchainExtension tcExt = project.getExtensions().getByType(ToolchainExtension.class);
    tcExt.registerPlatforms = false;

    project.getPluginManager().apply(PlatformRules.class);

    NamedDomainObjectContainer<ConfigurableCrossPlatformConfig> crossContainer = project.container(ConfigurableCrossPlatformConfig.class, name -> {
      return project.getObjects().newInstance(DefaultConfigurableCrossPlatformConfig.class, name);
    });
    project.getExtensions().add("configurableCrossCompilers", crossContainer);

    crossContainer.all(config -> {
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

    project.getExtensions().add("usePlatform", new UsePlatformHandler());
    project.getExtensions().add("useDesktop", new UseDesktopHandler(project));

    project.getExtensions().add(NativeUtils.class.getSimpleName(), NativeUtils.class.getName());
    project.getExtensions().add(ExportsConfig.class.getSimpleName(), ExportsConfig.class.getName());
    project.getExtensions().add(ExportsGenerationTask.class.getSimpleName(), ExportsGenerationTask.class.getName());
    project.getPluginManager().apply(ExportsConfigRules.class);
  }
}
