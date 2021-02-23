package edu.wpi.first.nativeutils.exports;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.SharedLibraryBinarySpecInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryTasks;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.OrderedStripTask;
import edu.wpi.first.toolchain.ToolchainRules;

public class PrivateExportsConfigRules extends RuleSource {

  @BinaryTasks
  public void createPrivateExportsSharedBinaryTasks(ModelMap<Task> tasks, SharedLibraryBinarySpecInternal binary) {
    if (!binary.getTargetPlatform().getOperatingSystem().isWindows()) {
      return;
    }

    Project project = binary.getBuildTask().getProject();
    NativeUtilsExtension nue = project.getExtensions().getByType(NativeUtilsExtension.class);

    PrivateExportsConfig config = nue.getPrivateExportsConfigs().findByName(binary.getComponent().getName());
    if (config == null) {
      return;
    }

    binary.getTasks().withType(AbstractLinkTask.class, link -> {
      String exportsTaskName = binary.getNamingScheme().getTaskName("generatePrivateExports");

      TaskProvider<PrivateExportsGenerationTask> exportsTask = project.getTasks().register(exportsTaskName,
          PrivateExportsGenerationTask.class, task -> {
            task.getSymbolsToExportFile().set(config.getExportsFile());
            task.getLibraryName().set(binary.getComponent().getBaseName());

            if (binary.getTargetPlatform().getOperatingSystem().isWindows()) {
              task.setIsWindows(true);
              task.getExportsFile()
                  .set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.def"));
              link.getLinkerArgs().add(task.getExportsFile().map(x -> "/DEF:" + x.getAsFile().toString()));
              link.getInputs().file(task.getExportsFile());

            } else if (binary.getTargetPlatform().getOperatingSystem().isMacOsX()) {
              task.setIsMac(true);
              task.getExportsFile()
                  .set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.txt"));
              link.getLinkerArgs()
                  .addAll(task.getExportsFile().map(x -> List.of("-exported_symbols_list", x.getAsFile().toString())));
              link.getInputs().file(task.getExportsFile());

            } else {
              task.getExportsFile()
                  .set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.txt"));
              link.getLinkerArgs()
                  .add(task.getExportsFile().map(x -> "-Wl,--version-script=" + x.getAsFile().toString()));
              link.getInputs().file(task.getExportsFile());
            }
          });

      link.dependsOn(exportsTask);
      binary.getTasks().add(exportsTask.get());

      if (config.getPerformStripAllSymbols().get()) {
        if (binary.getToolChain() instanceof GccToolChain) {
          GccToolChain gcc = (GccToolChain) binary.getToolChain();
          OrderedStripTask stripTask = ToolchainRules.configureOrderedStrip(link, gcc, binary);
          if (stripTask != null) {
            stripTask.setPerformStripAll(true);
          }
        }
      }
    });
  }
}
