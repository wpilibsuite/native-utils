package edu.wpi.first.nativeutils.exports;

import java.util.List;
import java.util.Map;

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
import org.gradle.nativeplatform.internal.SharedLibraryBinarySpecInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.ComponentSpecContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.toolchain.GccToolChain;
import edu.wpi.first.toolchain.OrderedStripTask;
import edu.wpi.first.toolchain.ToolchainExtension;

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

      TaskProvider<PrivateExportsGenerationTask> exportsTask = project.getTasks().register(exportsTaskName, PrivateExportsGenerationTask.class, task -> {
        task.getSymbolsToExportFile().set(config.getExportsFile());
            task.getLibraryName().set(binary.getComponent().getBaseName());

            if (binary.getTargetPlatform().getOperatingSystem().isWindows()) {
              task.setIsWindows(true);
              task.getExportsFile().set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.def"));
              link.getLinkerArgs().add(task.getExportsFile().map(x -> "/DEF:" + x.getAsFile().toString()));
              link.getInputs().file(task.getExportsFile());

            } else if (binary.getTargetPlatform().getOperatingSystem().isMacOsX()) {
              task.setIsMac(true);
              task.getExportsFile().set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.txt"));
              link.getLinkerArgs().addAll(task.getExportsFile().map(x -> List.of("-exported_symbols_list", x.getAsFile().toString())));
              link.getInputs().file(task.getExportsFile());

            } else {
              task.getExportsFile().set(project.getLayout().getBuildDirectory().file("tmp/" + exportsTaskName + "/exports.txt"));
              link.getLinkerArgs().add(task.getExportsFile().map(x -> "-Wl,--version-script=" + x.getAsFile().toString()));
              link.getInputs().file(task.getExportsFile());
            }
      });

      link.dependsOn(exportsTask);
      binary.getTasks().add(exportsTask.get());
    });
  }

  @Mutate
  void setupPrivateExports(ModelMap<Task> tasks, ExtensionContainer extensions, ProjectLayout projectLayout,
      ComponentSpecContainer components) {
    NamedDomainObjectCollection<PrivateExportsConfig> exports = extensions.getByType(NativeUtilsExtension.class)
        .getPrivateExportsConfigs();

    if (components == null) {
      return;
    }

    Project project = (Project) projectLayout.getProjectIdentifier();
    final ToolchainExtension tcExt = extensions.getByType(ToolchainExtension.class);

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

        Task rawLinkTask = binary.getTasks().getLink();

        // TODO move all of this to a binary task

        if (config.getPerformStripAllSymbols().get()) {
          if (rawLinkTask instanceof AbstractLinkTask) {
            AbstractLinkTask linkTask = (AbstractLinkTask) rawLinkTask;
            Map<AbstractLinkTask, OrderedStripTask> linkTaskMap = tcExt.getLinkTaskMap();
            OrderedStripTask stripTask = linkTaskMap.get(linkTask);
            if (stripTask == null) {

              GccToolChain gcc = null;
              if (binary.getToolChain() instanceof GccToolChain) {
                gcc = (GccToolChain) binary.getToolChain();
                stripTask = new OrderedStripTask(tcExt, binary, linkTask, gcc, project);
                linkTaskMap.put(linkTask, stripTask);
                linkTask.doLast(stripTask);
              }
            }

            if (stripTask != null) {
              stripTask.setPerformStripAll(true);
            }
          }
        }
      }
    }
  }
}
