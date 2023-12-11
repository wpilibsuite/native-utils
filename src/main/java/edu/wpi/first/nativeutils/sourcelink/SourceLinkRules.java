package edu.wpi.first.nativeutils.sourcelink;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.NativeBinarySpecInternal;
import org.gradle.nativeplatform.internal.NativeExecutableBinarySpecInternal;
import org.gradle.nativeplatform.internal.SharedLibraryBinarySpecInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryTasks;

public class SourceLinkRules extends RuleSource {

  @BinaryTasks
  public void createSourceLinkSharedBinaryTasks(ModelMap<Task> tasks, SharedLibraryBinarySpecInternal binary) {
    if (!binary.getTargetPlatform().getOperatingSystem().isWindows()) {
      return;
    }

    binary.getTasks().withType(AbstractLinkTask.class, link -> {
      handleLinkedComponent(binary, link, binary.getBuildTask().getProject().getRootProject().getTasks().named(SourceLinkPlugin.SOURCE_LINK_ROOT_TASK_NAME, SourceLinkGenerationTask.class), binary.getBuildTask().getProject());
    });
  }

  @BinaryTasks
  public void createSourceLinkExecutableTasks(ModelMap<Task> tasks, NativeExecutableBinarySpecInternal binary) {
    if (!binary.getTargetPlatform().getOperatingSystem().isWindows()) {
      return;
    }
    binary.getTasks().withType(AbstractLinkTask.class, link -> {
      handleLinkedComponent(binary, link, binary.getBuildTask().getProject().getRootProject().getTasks().named(SourceLinkPlugin.SOURCE_LINK_ROOT_TASK_NAME, SourceLinkGenerationTask.class), binary.getBuildTask().getProject());
    });
  }

  private void handleLinkedComponent(NativeBinarySpecInternal binary, AbstractLinkTask linkTask, TaskProvider<SourceLinkGenerationTask> rootGenTask, Project project) {
    String sourceLinkName = binary.getNamingScheme().getTaskName("generateSourceLinkFile");

    TaskProvider<LinkerSourceLinkGenerationTask> sourceGenTask = project.getTasks().register(sourceLinkName, LinkerSourceLinkGenerationTask.class);
    File sourceLinkFile = project.getLayout().getBuildDirectory().get().file("/tmp/" + sourceLinkName + "/SourceLink.json").getAsFile();
    sourceGenTask.configure(genTask -> {
        genTask.dependsOn(rootGenTask);
        Callable<Set<Object>> linkDepends = () -> linkTask.getLibs().getFrom();
        genTask.dependsOn(linkDepends);

        genTask.getInputFiles().add(rootGenTask.get().getSourceLinkBaseFile().getAsFile());

        genTask.getInputFiles().addAll(project.getProviders().provider(() -> linkTask.getLibs()));

        genTask.getSourceLinkFile().set(sourceLinkFile);
    });

    linkTask.dependsOn(sourceGenTask);
    linkTask.getLinkerArgs().add("/SOURCELINK:" + sourceLinkFile.getAbsolutePath());
    binary.getTasks().add(sourceGenTask.get());
  }
}
