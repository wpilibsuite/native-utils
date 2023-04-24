package edu.wpi.first.toolchain;

import java.io.File;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

public abstract class AbstractToolchainInstaller {

  public abstract void install(Project project);

  public abstract boolean targets(OperatingSystem os);

  public abstract File sysrootLocation();

  public boolean installable() {
    return targets(OperatingSystem.current());
  }
}
