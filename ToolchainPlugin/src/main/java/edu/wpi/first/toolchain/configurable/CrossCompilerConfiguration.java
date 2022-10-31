package edu.wpi.first.toolchain.configurable;

import javax.inject.Inject;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import edu.wpi.first.toolchain.ToolchainDescriptorBase;

public abstract class CrossCompilerConfiguration implements Named {
  private final String name;

  @Inject
  public CrossCompilerConfiguration(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Input
  public abstract Property<String> getArchitecture();
  @Input
  public abstract Property<String> getOperatingSystem();
  @Input
  public abstract Property<String> getCompilerPrefix();
  @Input
  public abstract Property<Boolean> getOptional();

  public abstract Property<ToolchainDescriptorBase> getToolchainDescriptor();
}
