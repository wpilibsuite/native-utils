package org.wpilib.nativeutils.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

public abstract class ResourceGenerationTask extends DefaultTask {
    private final ConfigurableFileCollection sourceFiles = getProject().getObjects().fileCollection();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Internal
    public abstract Property<String> getPrefix();

    @Internal
    public abstract Property<String> getNamespace();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @SkipWhenEmpty
    @InputFiles
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    public ConfigurableFileCollection getSource() {
        return sourceFiles;
    }

    @TaskAction
    public void execute(InputChanges changes) {
        WorkQueue workQueue = getWorkerExecutor().noIsolation();

        Map<File, String> filesToGenerate = new HashMap<>();

        Logger logger = Logging.getLogger(ResourceGenerationTask.class);

        for (FileChange change : changes.getFileChanges(sourceFiles)) {
            if (change.getFileType() == FileType.DIRECTORY) {
                continue;
            }
            String fixedRelativePath = change.getFile().getName().replaceAll("[^a-zA-Z0-9]", "_");
            if (change.getChangeType() == ChangeType.REMOVED) {
                File file = getOutputDirectory().file(fixedRelativePath + ".cpp").get().getAsFile();
                logger.info("Deleting {} because source {} was deleted", fixedRelativePath, change.getFile());
                file.delete();
            } else {

                logger.info("Generating {} from {} because of update", fixedRelativePath, change.getFile());
                filesToGenerate.put(change.getFile(), fixedRelativePath);
            }
        }

        String prefix = getPrefix().getOrElse("");
        String namespace = getNamespace().getOrElse("");

        for (Entry<File, String> inputFile : filesToGenerate.entrySet()) {
            String relativePath = inputFile.getValue();
            String funcName = "GetResource_";
            if (prefix != null && !prefix.isEmpty()) {
                funcName += prefix + "_";
            }
            funcName += relativePath;
            String finalFunc = funcName;
            Provider<RegularFile> generatedFile = getOutputDirectory().file(relativePath + ".cpp");
            workQueue.submit(ResourceGenerationAction.class, parameters -> {
                parameters.getSourceFile().set(inputFile.getKey());
                parameters.getOutputFile().set(generatedFile);
                parameters.getFuncName().set(finalFunc);
                parameters.getNamespace().set(namespace);
            });
        }
    }
}
