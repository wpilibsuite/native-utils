package edu.wpi.first.nativeutils.rules;

import java.util.Arrays;

import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.ComponentSpecContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.PrivateExportsConfig;
import edu.wpi.first.nativeutils.tasks.PrivateExportsGenerationTask;

public class PrivateExportsConfigRules extends RuleSource {

  @Mutate
  void setupPrivateExports(ModelMap<Task> tasks, ExtensionContainer extensions, ProjectLayout projectLayout,
      ComponentSpecContainer components) {
    NamedDomainObjectCollection<PrivateExportsConfig> exports = extensions.getByType(NativeUtilsExtension.class)
        .getPrivateExportsConfigs();

    if (components == null) {
      return;
    }

    Project project = (Project) projectLayout.getProjectIdentifier();

    for (ComponentSpec component : components) {
      PrivateExportsConfig config = exports.findByName(component.getName());
      if (config == null) {
        continue;
      }
      if (!(component instanceof NativeLibrarySpec)) {
        continue;
      }
      NativeLibrarySpec nativeComponent = (NativeLibrarySpec) component;
      for (BinarySpec oBinary : nativeComponent.getBinaries()) {
        if (!(oBinary instanceof SharedLibraryBinarySpec)) {
          continue;
        }
        SharedLibraryBinarySpec binary = (SharedLibraryBinarySpec) oBinary;
        String exportsTaskName = "generateExports" + binary.getBuildTask().getName();
        TaskProvider<PrivateExportsGenerationTask> exportsTask = project.getTasks().register(exportsTaskName, PrivateExportsGenerationTask.class, task -> {
          task.getSymbolsToExportFile().set(config.getExportsFile());
          task.getLibraryName().set(nativeComponent.getBaseName());
          if (binary.getTargetPlatform().getOperatingSystem().isWindows()) {
            task.setIsWindows(true);
            String exportsName = "exports.def";
            task.getExportsFile().set(task.getProject().file(task.getProject().getBuildDir() + "/tmp/" + task.getName() + "/" + exportsName));
            ((LinkSharedLibrary)binary.getTasks().getLink()).getLinkerArgs().add(project.provider(() -> {
              return "/DEF:" + task.getExportsFile().get().getAsFile().toString();
            }));
            binary.getTasks().getLink().getInputs().file(task.getExportsFile());
          } else if (binary.getTargetPlatform().getOperatingSystem().isMacOsX()) {
            String exportsName = "exports.txt";
            task.getExportsFile().set(task.getProject().file(task.getProject().getBuildDir() + "/tmp/" + task.getName() + "/" + exportsName));
            ((LinkSharedLibrary)binary.getTasks().getLink()).getLinkerArgs().addAll(project.provider(() -> {
              return Arrays.asList("-exported_symbols_list", task.getExportsFile().get().getAsFile().toString());
            }));
            binary.getTasks().getLink().getInputs().file(task.getExportsFile());
          } else {
            String exportsName = "exports.txt";
            task.getExportsFile().set(task.getProject().file(task.getProject().getBuildDir() + "/tmp/" + task.getName() + "/" + exportsName));
            ((LinkSharedLibrary)binary.getTasks().getLink()).getLinkerArgs().add(project.provider(() -> {
              return "-Wl,--version-script=" + task.getExportsFile().get().getAsFile().toString();
            }));
            binary.getTasks().getLink().getInputs().file(task.getExportsFile());
          }

        });
        binary.getTasks().getLink().dependsOn(exportsTask);
      }
    }
  }
}
