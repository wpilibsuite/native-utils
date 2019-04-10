package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import edu.wpi.first.toolchain.ToolchainExtension;

public class WPINative implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(NativeUtils.class);
    ToolchainExtension tcExt = project.getExtensions().getByType(ToolchainExtension.class);
    tcExt.withRaspbian();
    tcExt.withRoboRIO();

    NativeUtilsExtension nativeExt = project.getExtensions().getByType(NativeUtilsExtension.class);

    project.getExtensions().create("wpiNative", WPINativeExtension.class, nativeExt);
  }

}
