package edu.wpi.first.nativeutils.exports;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.internal.os.OperatingSystem;

public class ExportsConfigPlugin implements Plugin<Project> {

  public static final String EXPORT_GENERATOR_EXTRACT_TASK_NAME = "extractGeneratorTask";

  @Override
  public void apply(Project project) {
    if (!OperatingSystem.current().isWindows()) {
      return;
    }
    Project rootProject = project.getRootProject();

    project.getPluginManager().apply(ExportsConfigRules.class);

    try {
      rootProject.getTasks().named(EXPORT_GENERATOR_EXTRACT_TASK_NAME);
    } catch (UnknownTaskException un) {
      rootProject
          .getTasks()
          .register(
              EXPORT_GENERATOR_EXTRACT_TASK_NAME,
              ExtractDefFileGeneratorTask.class,
              task -> {
                task.getOutputs().file(task.getDefFileGenerator());
                task.getDefFileGenerator()
                    .set(rootProject.getLayout().getBuildDirectory().file("DefFileGenerator.exe"));
              });
    }
  }
}
