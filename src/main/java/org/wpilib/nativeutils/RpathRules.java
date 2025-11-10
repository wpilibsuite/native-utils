package edu.wpi.first.nativeutils;

import org.gradle.api.Task;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.platform.base.BinaryTasks;

public class RpathRules extends RuleSource {
  @BinaryTasks
  public void createRpathSharedBinaryTasks(ModelMap<Task> tasks, SharedLibraryBinarySpec binary) {
    if (!binary.getTargetPlatform().getOperatingSystem().isMacOsX()) {
      return;
    }

    binary.getTasks().withType(LinkSharedLibrary.class, link -> {
        link.getInstallName().set("@rpath/" + binary.getSharedLibraryFile().getName());
    });
  }
}
