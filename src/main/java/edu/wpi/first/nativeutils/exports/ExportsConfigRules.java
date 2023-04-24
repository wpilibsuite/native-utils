package edu.wpi.first.nativeutils.exports;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.SharedLibraryBinarySpecInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryTasks;

public class ExportsConfigRules extends RuleSource {

  @BinaryTasks
  public void createExportsSharedBinaryTasks(
      ModelMap<Task> tasks, SharedLibraryBinarySpecInternal binary) {
    if (!binary.getTargetPlatform().getOperatingSystem().isWindows()) {
      return;
    }

    Project project = binary.getBuildTask().getProject();
    NativeUtilsExtension nue = project.getExtensions().getByType(NativeUtilsExtension.class);

    ExportsConfig config = nue.getExportsConfigs().findByName(binary.getComponent().getName());
    if (config == null) {
      return;
    }

    binary
        .getTasks()
        .withType(
            AbstractLinkTask.class,
            link -> {
              TaskProvider<ExtractDefFileGeneratorTask> extractTask =
                  binary
                      .getBuildTask()
                      .getProject()
                      .getRootProject()
                      .getTasks()
                      .named(
                          ExportsConfigPlugin.EXPORT_GENERATOR_EXTRACT_TASK_NAME,
                          ExtractDefFileGeneratorTask.class);

              String exportsName = binary.getNamingScheme().getTaskName("generateExports");

              TaskProvider<ExportsGenerationTask> exportsTask =
                  project
                      .getTasks()
                      .register(
                          exportsName,
                          ExportsGenerationTask.class,
                          task -> {
                            task.setArchitecture(
                                binary.getTargetPlatform().getArchitecture().getName());
                            task.setExportsConfig(config);
                            task.getDefFileGenerator().set(extractTask.get().getDefFileGenerator());

                            task.getSourceFiles()
                                .from(link.getSource().filter(x -> x.getName().endsWith(".obj")));

                            task.getDefFile()
                                .set(
                                    project
                                        .getLayout()
                                        .getBuildDirectory()
                                        .file("tmp/" + exportsName + "/exports.def"));
                            link.getLinkerArgs()
                                .add(
                                    task.getDefFile().map(x -> "/DEF:" + x.getAsFile().toString()));

                            task.dependsOn(extractTask.get());

                            link.getInputs().file(task.getDefFile());
                          });

              link.dependsOn(exportsTask);
              binary.getTasks().add(exportsTask.get());
            });
  }
}
