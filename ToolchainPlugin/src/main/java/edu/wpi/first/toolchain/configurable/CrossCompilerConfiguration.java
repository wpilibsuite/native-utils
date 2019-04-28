package edu.wpi.first.toolchain.configurable;

import org.gradle.api.Named;

import edu.wpi.first.toolchain.ToolchainDescriptorBase;

public interface CrossCompilerConfiguration extends Named {
  void setArchitecture(String arch);
  String getArchitecture();

  void setOperatingSystem(String os);
  String getOperatingSystem();

  void setCompilerPrefix(String prefix);
  String getCompilerPrefix();

  void setOptional(boolean optional);
  boolean getOptional();

  void setToolchainDescriptor(ToolchainDescriptorBase descriptor);
  ToolchainDescriptorBase getToolchainDescriptor();
}
