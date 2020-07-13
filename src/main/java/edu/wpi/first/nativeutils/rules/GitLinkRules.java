package edu.wpi.first.nativeutils.rules;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.test.NativeTestSuiteBinarySpec;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.tasks.LinkerSourceLinkGenerationTask;
import edu.wpi.first.nativeutils.tasks.SourceLinkGenerationTask;

public class GitLinkRules extends RuleSource {


  private void handleLinkedComponent(NativeBinarySpec binary, AbstractLinkTask linkTask, TaskProvider<SourceLinkGenerationTask> rootGenTask, Project project) {

    String sourceLinkName = "generateSourceLink" + binary.getBuildTask().getName();

    TaskProvider<LinkerSourceLinkGenerationTask> sourceGenTask = project.getTasks().register(sourceLinkName, LinkerSourceLinkGenerationTask.class);
    File tmpDir = project.file(project.getBuildDir() + "/tmp/" + sourceLinkName);
    File sourceLinkFile = project.file(tmpDir.toString() + "/SourceLink.json");
    sourceGenTask.configure(new Action<LinkerSourceLinkGenerationTask>() {
      @Override
      public void execute(LinkerSourceLinkGenerationTask genTask) {
        genTask.dependsOn(rootGenTask);
        genTask.getInputFiles().add(rootGenTask.get().getSourceLinkBaseFile().getAsFile());
        genTask.getInputFiles().addAll(linkTask.getLibs());

        genTask.getSourceLinkFile().set(sourceLinkFile);

      }
    });

    linkTask.dependsOn(sourceGenTask);
    linkTask.getLinkerArgs().add("/SOURCELINK:" + sourceLinkFile.getAbsolutePath());
  }

  @Mutate
  void setupGitLink(ModelMap<Task> tasks, BinaryContainer binaries, ExtensionContainer extensions, ProjectLayout projectLayout) {
    if (binaries == null) {
      return;
    }

    if (!OperatingSystem.current().isWindows()) {
      // It's impossible for this to run on non windows
      return;
    }

    Project project = (Project)projectLayout.getProjectIdentifier();

    NativeUtilsExtension ext = extensions.getByType(NativeUtilsExtension.class);
    TaskProvider<SourceLinkGenerationTask> genTask = ext.getSourceLinkTask();
    boolean setupSourceLink = genTask != null;

    if (!setupSourceLink) {
      // Return if neither is to be set up
      return;
    }

    for (BinarySpec binary : binaries) {
      // Find the Link task
      if (!binary.isBuildable()) {
        continue;
      }

      if (binary instanceof SharedLibraryBinarySpec && setupSourceLink) {
        SharedLibraryBinarySpec s = (SharedLibraryBinarySpec)binary;
        if (!s.getTargetPlatform().getOperatingSystem().isWindows()) {
          continue;
        }
        Task l = s.getTasks().getLink();
        if (l instanceof AbstractLinkTask) {
          handleLinkedComponent(s, (AbstractLinkTask)l, genTask, project);
        }
      } else if (binary instanceof NativeExecutableBinarySpec && setupSourceLink) {
        NativeExecutableBinarySpec s = (NativeExecutableBinarySpec)binary;
        if (!s.getTargetPlatform().getOperatingSystem().isWindows()) {
          continue;
        }
        Task l = s.getTasks().getLink();
        if (l instanceof AbstractLinkTask) {
          handleLinkedComponent(s, (AbstractLinkTask)l, genTask, project);
        }
      } else if (binary instanceof NativeTestSuiteBinarySpec && setupSourceLink) {
        NativeTestSuiteBinarySpec s = (NativeTestSuiteBinarySpec)binary;
        if (!s.getTargetPlatform().getOperatingSystem().isWindows()) {
          continue;
        }
        Task l = s.getTasks().getLink();
        if (l instanceof AbstractLinkTask) {
          handleLinkedComponent(s, (AbstractLinkTask)l, genTask, project);
        }
      }
    }
  }
}
