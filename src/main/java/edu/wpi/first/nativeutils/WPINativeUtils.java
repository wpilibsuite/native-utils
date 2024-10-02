package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.LinkExecutable;

public class WPINativeUtils implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(NativeUtils.class);

    NativeUtilsExtension nativeExt = project.getExtensions().getByType(NativeUtilsExtension.class);

    nativeExt.addWpiExtension();

    project.getPluginManager().apply(RpathRules.class);

    if (project.hasProperty("developerID")) {
      project.getTasks().withType(AbstractLinkTask.class).forEach((task) -> {
            // Don't sign any executables because codesign complains
            // about relative rpath.
            if (!(task instanceof LinkExecutable)) {
              // Get path to binary.
              String path = task.getLinkedFile().getAsFile().get().getAbsolutePath();
              ProcessBuilder builder = new ProcessBuilder();
              var codesigncommand = String.format("codesign --force --strict --timestamp --options=runtime "
              + "--version -s %s %s", project.findProperty("developerID"), path);
              builder.command("sh", "-c", codesigncommand);
              builder.directory(project.getRootDir());
            }
        });
      }
    }
}
