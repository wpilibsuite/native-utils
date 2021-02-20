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
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.internal.AbstractNativeLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpec;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.binary.BaseBinarySpec;

import de.undercouch.gradle.tasks.download.org.apache.commons.codec.binary.StringUtils;
import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.tasks.ExportsGenerationTask;
import edu.wpi.first.nativeutils.tasks.ExtractDefFileGeneratorTask;

public class ExportsConfigRules extends RuleSource {

    @Mutate
    void setupExports(ComponentSpecContainer components, ExtensionContainer extensions, ProjectLayout projectLayout) {
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
        TaskProvider<ExtractDefFileGeneratorTask> exportsGenTask;

        try {
            exportsGenTask = rootProject.getTasks().named(extractGeneratorTaskName, ExtractDefFileGeneratorTask.class);
        } catch (UnknownTaskException notFound) {
            exportsGenTask = rootProject.getTasks().register(extractGeneratorTaskName,
                    ExtractDefFileGeneratorTask.class, task -> {
                        task.getOutputs().file(task.getDefFileGenerator());
                        task.getDefFileGenerator()
                                .set(rootProject.getLayout().getBuildDirectory().file("DefFileGenerator.exe"));
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

        TaskProvider<ExtractDefFileGeneratorTask> finalExportsTask = exportsGenTask;

        for (ExportsConfig config : exports) {
            components.named(config.getName(), component -> {
                if (component instanceof NativeLibrarySpec) {
                    ((NativeLibrarySpec) component).getBinaries().withType(SharedLibraryBinarySpec.class, binary -> {
                        List<String> excludeBuildTypes = config.getExcludeBuildTypes().get();
                        if (binary.getTargetPlatform().getOperatingSystem().isWindows()
                                && !excludeBuildTypes.contains(binary.getBuildType().getName())) {

                            String exportsTaskName = "generateExports" + binary.getComponent().getName()
                                    + binary.getName();

                            binary.getTasks().withType(AbstractLinkTask.class, linkTask -> {
                                TaskProvider<ExportsGenerationTask> exportsTask = project.getTasks()
                                        .register(exportsTaskName, ExportsGenerationTask.class, task -> {
                                            task.setInternal(binary, config);
                                            task.getDefFileGenerator().set(finalExportsTask.get().getDefFileGenerator());
                                            task.getSourceFiles().setFrom(linkTask.getSource());
                                            task.getDefFile().set(project.getLayout().getBuildDirectory()
                                                    .file("tmp/" + exportsTaskName + "/exports.def"));
                                            linkTask.getLinkerArgs()
                                                    .add(task.getDefFile().map(x -> "/DEF:" + x.getAsFile().toString()));
                                            task.dependsOn(rootProject.getTasks().named(extractGeneratorTaskName));
                                            task.dependsOn(finalExportsTask);

                                            binary.getTasks().add(task);
                                            binary.getTasks().withType(AbstractNativeSourceCompileTask.class)
                                                    .configureEach(it -> {
                                                        task.dependsOn(it);
                                                    });

                                        });

                                exportsTask.configure(task -> {
                                    for (Object o : linkTask.getDependsOn()) {
                                        if (exportsTask == o) {
                                            continue;
                                        }
                                        System.out.println(o);
                                        task.dependsOn(o);
                                    }
                                });

                                linkTask.dependsOn(exportsTask);
                            });

                            // binary.getTasks().add(exportsTask);
                            // binary.getTasks().withType(AbstractNativeSourceCompileTask.class).configureEach(it
                            // -> {
                            // exportsTask.dependsOn(it);
                            // });

                            // Task linkTask = binary.getTasks().getLink();

                            //
                            // linkTask.dependsOn(exportsTask);
                        }

                    });

                }
            });
        }
    }
}
