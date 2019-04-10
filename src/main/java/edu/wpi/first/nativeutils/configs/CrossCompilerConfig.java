package edu.wpi.first.nativeutils.configs;

import org.gradle.api.Named;

public interface CrossCompilerConfig extends Named {
  void setArchitecture(String arch);
  String getArchitecture();

  void setOperatingSystem(String os);
  String getOperatingSystem();

  void setCompilerPrefix(String prefix);
  String getCompilerPrefix();

  void setOptional(boolean optional);
  boolean getOptional();
}
