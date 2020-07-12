package edu.wpi.first.nativeutils.rules;

import java.io.ByteArrayOutputStream;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.process.ExecSpec;

public class GitLinkRules extends RuleSource {

  @Mutate
  void setupGitLink(ModelMap<Task> tasks, BinaryContainer binaries, ProjectLayout projectLayout) {
    // Test executable
    Project project = (Project)projectLayout.getProjectIdentifier();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      project.exec(new Action<ExecSpec>() {
        @Override
        public void execute(ExecSpec spec) {
          spec.executable("GitLink.exe");
          spec.setIgnoreExitValue(true);
          spec.setStandardOutput(os);
          spec.setErrorOutput(os);
        }

      });
    } catch (Exception e) {
      return; // Can't execute, do nothing
    }

    if (binaries == null) {
      return;
    }

    for (BinarySpec binary : binaries) {
      // Find the Link task
      AbstractLinkTask linkTask = null;
      if (!binary.isBuildable()) {
        continue;
      }
      if (binary instanceof SharedLibraryBinarySpec) {
        SharedLibraryBinarySpec s = (SharedLibraryBinarySpec)binary;
        if (!s.getTargetPlatform().getOperatingSystem().isWindows()) {
          continue;
        }
        Task l = s.getTasks().getLink();
        if (l instanceof AbstractLinkTask) {
          linkTask = (AbstractLinkTask)l;
        }
      } else if (binary instanceof NativeExecutableBinarySpec) {
        NativeExecutableBinarySpec s = (NativeExecutableBinarySpec)binary;
        if (!s.getTargetPlatform().getOperatingSystem().isWindows()) {
          continue;
        }
        Task l = s.getTasks().getLink();
        if (l instanceof AbstractLinkTask) {
          linkTask = (AbstractLinkTask)l;
        }
      } else {
        continue;
      }

      AbstractLinkTask finalLinkTask = linkTask;

      linkTask.doLast(new Action<Task>() {

        @Override
        public void execute(Task arg0) {
          String sharedPath = finalLinkTask.getLinkedFile().get().getAsFile().getAbsolutePath();
          sharedPath = sharedPath.substring(0, sharedPath.length() - 4);
          String pdb = sharedPath + ".pdb";
          project.exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec spec) {
              spec.executable("GitLink.exe");
              spec.args(pdb, "-a");
            }

          });
        }
      });
    }
  }
}
