package edu.wpi.first.nativeutils.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.ComponentSpecContainer;

import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.specs.ExportsConfigSpec;
import edu.wpi.first.nativeutils.storage.ExportsStorage;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import groovy.lang.Closure;

public class ExportsConfigRules extends RuleSource {

  @Model("exportsConfigs")
  void createExportsConfigs(ExportsConfigSpec configs) {}

  @Validate
  void setupExports(ModelMap<Task> tasks, ExportsConfigSpec configs, ProjectLayout projectLayout, ComponentSpecContainer components) {
    if (!OperatingSystem.current().isWindows()) {
        return;
    }

    if (configs == null) {
        return;
    }

    if (components == null) {
        return;
    }

    Project project = (Project)projectLayout.getProjectIdentifier();

    for (ExportsConfig config : configs) {
      for (ComponentSpec component : components) {
        if (component.getName().equals(config.getName())) {
          if (component instanceof NativeLibrarySpec) {
            for (BinarySpec oBinary : ((NativeLibrarySpec)component).getBinaries()) {
              NativeBinarySpec binary = (NativeBinarySpec)oBinary;
              List<String> excludeBuildTypes = config.getExcludeBuildTypes() == null ? new ArrayList<>() : config.getExcludeBuildTypes();
              if (binary.getTargetPlatform().getOperatingSystem().isWindows() &&
                  binary instanceof SharedLibraryBinarySpec &&
                  !excludeBuildTypes.contains(binary.getBuildType().getName())) {
                SharedLibraryBinarySpec sBinary = (SharedLibraryBinarySpec)binary;

                String exportsTaskName = "generateExports" + binary.getBuildTask().getName();

                Task exportsTask = project.getTasks().create(exportsTaskName, ExportsGenerationTask.class, task -> {
                  task.getInputs().files(((AbstractLinkTask)sBinary.getTasks().getLink()).getSource());
                  File tmpDir = project.file(project.getBuildDir() + "/tmp/" + exportsTaskName);
                  File defFile = project.file(tmpDir.toString() + "/exports.def");
                  sBinary.getLinker().args("/DEF:" + defFile.toString());
                  sBinary.getTasks().getLink().getInputs().file(defFile);
                  task.getOutputs().file(defFile);

                  task.doLast(last -> {
                    tmpDir.mkdirs();
                    String exeName = ExportsStorage.getGeneratorFilePath();
                    project.exec(exec -> {
                      exec.setExecutable(exeName);
                      exec.args(defFile);
                      exec.args(((AbstractLinkTask)sBinary.getTasks().getLink()).getSource());
                    });

                    final List<String> lines = new ArrayList<>();
                    List<String> excludeSymbols;
                    boolean isX86 = sBinary.getTargetPlatform().getArchitecture().getName().equals("x86");
                    if (isX86) {
                      excludeSymbols = config.getX86ExcludeSymbols();
                    } else {
                      excludeSymbols = config.getX64ExcludeSymbols();
                    }

                    if (excludeSymbols == null) {
                      excludeSymbols = new ArrayList<>();
                    }
                    final List<String> exSymbols = excludeSymbols;
                    try (Stream<String> stream = Files.lines(defFile.toPath())) {
                      stream.map(s -> s.trim()).forEach(line -> {
                        String symbol = line;
                        int space = line.indexOf(' ');
                        if (space != -1) {
                          symbol = symbol.substring(0, space);
                        }
                        if (!symbol.equals("EXPORTS") && !exSymbols.contains(symbol)) {
                          lines.add(symbol);
                        }
                      });
                    } catch (IOException ex) {

                    }

                    if (isX86) {
                      Closure symbolFilter = config.getX86SymbolFilter();
                      if (symbolFilter != null) {
                        List<String> tmpLines = (List<String>)symbolFilter.call(lines);
                        lines.clear();
                        lines.addAll(tmpLines);
                      }
                    } else {
                      Closure symbolFilter = config.getX64SymbolFilter();
                      if (symbolFilter != null) {
                        List<String> tmpLines = (List<String>)symbolFilter.call(lines);
                        lines.clear();
                        lines.addAll(tmpLines);
                      }
                    }

                    try (BufferedWriter writer = Files.newBufferedWriter(defFile.toPath()))
                    {
                        writer.append("EXPORTS").append('\n');
                        for (String line : lines) {
                          writer.append(line).append('\n');
                        }
                    } catch (IOException ex) {
                    }
                  });
                });

                sBinary.getTasks().withType(AbstractNativeSourceCompileTask.class, it -> {
                  exportsTask.dependsOn(it);
                });

                Task linkTask = sBinary.getTasks().getLink();

                for (Object o : linkTask.getDependsOn()) {
                  exportsTask.dependsOn(o);
                }
                linkTask.dependsOn(exportsTask);
              }
            }
          }
        }
      }
    }
  }

  @Mutate
  void doThingWithExports(ModelMap<Task> tasks, ExportsConfigSpec exports) {}
}
