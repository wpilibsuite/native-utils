package edu.wpi.first.nativeutils;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.TargetedNativeComponent;

import edu.wpi.first.toolchain.NativePlatforms;

public class UseDesktopHandler implements Action<TargetedNativeComponent> {
  private Project project;

  public UseDesktopHandler(Project project) {
    this.project = project;
  }


  @Override
  public void execute(TargetedNativeComponent component) {
    if (!OperatingSystem.current().isWindows()) {
      // Only support 64 bit
      component.targetPlatform(NativePlatforms.desktop);
    } else if (project.hasProperty("buildBothWindowsPlatforms")) {
      String arch = System.getProperty("os.arch");
      if (!arch.equals("amd64") && !arch.equals("x86_64")) {
        throw new GradleException("Both platforms only supported on 64 bit windows");
      }
      // Is windows, can support 32 bit
      component.targetPlatform(NativePlatforms.desktopOS() + "x86");
      component.targetPlatform(NativePlatforms.desktop);
    }  else {
      // Only windows 64 bit
      component.targetPlatform(NativePlatforms.desktop);
    }
  }

}
