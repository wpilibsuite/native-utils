package edu.wpi.first.nativeutils.rules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.ComponentSpecContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import edu.wpi.first.nativeutils.tasks.ExtractDefFileGeneratorTask;

public class ExportsConfigRules extends RuleSource {

    @Mutate
    void setupExports(ModelMap<Task> tasks, ExtensionContainer extensions, ProjectLayout projectLayout,
            ComponentSpecContainer components) {
        NamedDomainObjectCollection<ExportsConfig> exports = extensions.getByType(NativeUtilsExtension.class)
                .getExportsConfigs();
        if (!OperatingSystem.current().isWindows()) {
            return;
        }

        if (components == null) {
            return;
        }

        Project project = (Project) projectLayout.getProjectIdentifier();
        Project rootProject = project.getRootProject();

        String extractGeneratorTaskName = "extractGeneratorTask";

        try {
            rootProject.getTasks().named(extractGeneratorTaskName);
        } catch (UnknownTaskException notFound) {
            rootProject.getTasks().register(extractGeneratorTaskName, ExtractDefFileGeneratorTask.class, task -> {
                task.getOutputs().file(task.getDefFileGenerator());
                task.getDefFileGenerator().set(rootProject.getLayout().getBuildDirectory().file("DefFileGenerator.exe"));
                task.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task t) {
                        File file = task.getDefFileGenerator().getAsFile().get();
                        InputStream is = ExportsConfigRules.class.getResourceAsStream("/DefFileGenerator.exe");
                        OutputStream os = null;

                        byte[] buffer = new byte[1024];
                        int readBytes;
                        try {
                            os = new FileOutputStream(file);
                            while ((readBytes = is.read(buffer)) != -1) {
                                os.write(buffer, 0, readBytes);
                            }
                        } catch (IOException ex) {
                        } finally {
                            try {
                                if (os != null) {
                                    os.close();
                                }
                                is.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                });
            });
        }

        for (ComponentSpec component : components) {
            ExportsConfig config = exports.findByName(component.getName());
            if (config == null) {
                continue;
            }
            if (component instanceof NativeLibrarySpec) {
                for (BinarySpec oBinary : ((NativeLibrarySpec) component).getBinaries()) {
                    NativeBinarySpec binary = (NativeBinarySpec) oBinary;
                    List<String> excludeBuildTypes = config.getExcludeBuildTypes().get();
                    if (binary.getTargetPlatform().getOperatingSystem().isWindows()
                            && binary instanceof SharedLibraryBinarySpec
                            && !excludeBuildTypes.contains(binary.getBuildType().getName())) {
                        SharedLibraryBinarySpec sBinary = (SharedLibraryBinarySpec) binary;

                        String exportsTaskName = "generateExports" + binary.getBuildTask().getName();

                        ExportsGenerationTask exportsTask = project.getTasks().create(exportsTaskName,
                                ExportsGenerationTask.class, task -> {
                                    task.getInputs()
                                            .files(((AbstractLinkTask) sBinary.getTasks().getLink()).getSource());
                                    File tmpDir = project.file(project.getBuildDir() + "/tmp/" + exportsTaskName);
                                    File defFile = project.file(tmpDir.toString() + "/exports.def");
                                    sBinary.getLinker().args("/DEF:" + defFile.toString());
                                    sBinary.getTasks().getLink().getInputs().file(defFile);
                                    task.getOutputs().file(defFile);
                                    task.dependsOn(rootProject.getTasks().named(extractGeneratorTaskName));

                                    task.doLast(new Action<Task>() {
                                        @Override
                                        public void execute(Task last) {
                                            tmpDir.mkdirs();
                                            ExtractDefFileGeneratorTask extractTask = (ExtractDefFileGeneratorTask) rootProject
                                                    .getTasks().named(extractGeneratorTaskName).get();
                                            String exeName = extractTask.getDefFileGenerator().getAsFile().get().toString();
                                            project.exec(exec -> {
                                                exec.setExecutable(exeName);
                                                exec.args(defFile);
                                                exec.args(
                                                        ((AbstractLinkTask) sBinary.getTasks().getLink()).getSource());
                                            });

                                            final List<String> lines = new ArrayList<>();
                                            List<String> excludeSymbols;
                                            boolean isX86 = sBinary.getTargetPlatform().getArchitecture().getName()
                                                    .equals("x86");
                                            if (isX86) {
                                                excludeSymbols = config.getX86ExcludeSymbols().get();
                                            } else {
                                                excludeSymbols = config.getX64ExcludeSymbols().get();
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
                                                Action<List<String>> symbolFilter = config.getX86SymbolFilter().getOrElse(null);
                                                if (symbolFilter != null) {
                                                    symbolFilter.execute(lines);
                                                }
                                            } else {
                                                Action<List<String>> symbolFilter = config.getX64SymbolFilter().getOrElse(null);
                                                if (symbolFilter != null) {
                                                    symbolFilter.execute(lines);
                                                }
                                            }

                                            try (BufferedWriter writer = Files.newBufferedWriter(defFile.toPath())) {
                                                writer.append("EXPORTS").append('\n');
                                                for (String line : lines) {
                                                    writer.append(line).append('\n');
                                                }
                                            } catch (IOException ex) {
                                            }
                                        }
                                    });
                                });

                        sBinary.getTasks().add(exportsTask);
                        sBinary.getTasks().withType(AbstractNativeSourceCompileTask.class).configureEach(it -> {
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
