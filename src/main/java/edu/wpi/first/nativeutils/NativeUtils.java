package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.rules.CrossPlatformRules;
import edu.wpi.first.nativeutils.rules.ExportsConfigRules;
import edu.wpi.first.nativeutils.rules.NativePlatformRules;
import edu.wpi.first.nativeutils.rules.PlatformRules;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import edu.wpi.first.toolchain.ToolchainPlugin;

public class NativeUtils implements Plugin<Project> {
  @Override
  public void apply(Project project) {

    project.getPluginManager().apply(ToolchainPlugin.class);
    project.getPluginManager().apply(PlatformRules.class);
    project.getPluginManager().apply(CrossPlatformRules.class);
    project.getPluginManager().apply(NativePlatformRules.class);

    project.getExtensions().add("usePlatform", new UsePlatformHandler());

    project.getExtensions().add(NativeUtils.class.getSimpleName(), NativeUtils.class.getName());
    project.getExtensions().add(ExportsConfig.class.getSimpleName(), ExportsConfig.class.getName());
    project.getExtensions().add(ExportsGenerationTask.class.getSimpleName(), ExportsGenerationTask.class.getName());
    project.getPluginManager().apply(ExportsConfigRules.class);
  }
}
