package edu.wpi.first.nativeutils.pdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.StaticLibraryBinarySpecInternal;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;
import org.gradle.nativeplatform.tasks.InstallExecutable;
import org.gradle.platform.base.BinaryTasks;

public class PdbPlugin extends RuleSource {
    private void staticLibraryPdbConfiguration(ModelMap<Task> tasks, StaticLibraryBinarySpecInternal staticLib) {
        // Static libraries are special. Their pdb's are handled differently. To solve
        // this, we need to special case some pdbs
        // First, rename the output location
        if (!staticLib.getTargetPlatform().getOperatingSystem().isWindows()) {
            return;
        }

        staticLib.getTasks().withType(CreateStaticLibrary.class, lib -> {
            CreateStaticLibrary create = (CreateStaticLibrary) lib;
            File libFile = create.getOutputFile().get().getAsFile();
            File pdbRoot = libFile.getParentFile();

            String makePdbDirName = staticLib.getNamingScheme().getTaskName("makePdbDirFor");

            tasks.create(makePdbDirName, task -> {
                task.getOutputs().upToDateWhen(new Spec<Task>() {

                    @Override
                    public boolean isSatisfiedBy(Task arg0) {
                        return pdbRoot.exists();
                    }

                });

                staticLib.getTasks().withType(CppCompile.class).configureEach(it -> {
                    String pdbFile = new File(pdbRoot, it.getName() + ".pdb").getAbsolutePath();
                    it.getCompilerArgs().add("/Fd:" + pdbFile);
                    it.dependsOn(task);
                    it.getOutputs().file(pdbFile);
                });

                staticLib.getTasks().withType(CCompile.class).configureEach(it -> {
                    String pdbFile = new File(pdbRoot, it.getName() + ".pdb").getAbsolutePath();
                    it.getCompilerArgs().add("/Fd:" + pdbFile);
                    it.dependsOn(task);
                    it.getOutputs().file(pdbFile);
                });

                task.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task arg0) {
                        pdbRoot.mkdirs();
                    }
                });
            });
        });
    }

    @Mutate
    public void createPdbStaticBinaryTasks(ModelMap<Task> tasks) {
        tasks.withType(InstallExecutable.class, install -> {
            install.doFirst(new Action<Task>() {

                @Override
                public void execute(Task installTaskRaw) {
                    InstallExecutable installTask = (InstallExecutable) installTaskRaw;
                    List<File> filesToAdd = new ArrayList<>();
                    for (File file : installTask.getLibs()) {
                        if (file.exists()) {
                            String name = file.getName();
                            name = name.substring(0, name.length() - 3);
                            File pdbFile = new File(file.getParentFile(), name + "pdb");
                            if (pdbFile.exists()) {
                                filesToAdd.add(pdbFile);
                            }
                        }
                    }
                    File toInstallFile = installTask.getExecutableFile().get().getAsFile();
                    String toInstallName = toInstallFile.getName();
                    toInstallName = toInstallName.substring(0, toInstallName.length() - 3);
                    File exePdbFile = new File(toInstallFile.getParentFile(), toInstallName + "pdb");
                    if (exePdbFile.exists()) {
                        filesToAdd.add(exePdbFile);
                    }
                    installTask.lib(filesToAdd);
                }

            });
        });
    }

    @BinaryTasks
    public void createPdbStaticBinaryTasks(ModelMap<Task> tasks, StaticLibraryBinarySpecInternal binary) {
        if (!binary.getTargetPlatform().getOperatingSystem().isWindows()) {
            return;
        }
        staticLibraryPdbConfiguration(tasks, binary);
    }
}
