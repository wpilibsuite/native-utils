package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WPINativeUtils implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(NativeUtils.class);

    NativeUtilsExtension nativeExt = project.getExtensions().getByType(NativeUtilsExtension.class);

    nativeExt.addWpiExtension();
  }
}
