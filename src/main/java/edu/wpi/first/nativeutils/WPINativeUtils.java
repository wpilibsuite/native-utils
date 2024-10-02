package edu.wpi.first.nativeutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.options.Option;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.LinkExecutable;

public class WPINativeUtils implements Plugin<Project> {
  private String developerID = null;

  @Option(option = "sign", description = "Sign with developer ID (MacOS only)")
  public void sign(String developerID) {
    if (!System.getProperty("os.name").startsWith("Mac")) {
      throw new RuntimeException("Can't sign binaries on non mac platforms");
    }

    this.developerID = developerID;
  }

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(NativeUtils.class);

    NativeUtilsExtension nativeExt = project.getExtensions().getByType(NativeUtilsExtension.class);

    nativeExt.addWpiExtension();

    project.getPluginManager().apply(RpathRules.class);

    if (System.getProperty("os.name").startsWith("Mac")) {
      if (developerID != null) {
        project.getTasks().withType(AbstractLinkTask.class).forEach((task) -> {
              // Don't sign any executables because codesign complains
              // about relative rpath.
              if (!(task instanceof LinkExecutable)) {
                // Get path to binary.
                String path = task.getLinkedFile().getAsFile().get().getAbsolutePath();
                ProcessBuilder builder = new ProcessBuilder();
                var codesigncommand = String.format("codesign --force --strict --timestamp --options=runtime "
                + "--version -s %s %s", developerID, path);
                builder.command("sh", "-c", codesigncommand);
                builder.directory(project.getRootDir());
              }
          });
        }
      }
    }
}
