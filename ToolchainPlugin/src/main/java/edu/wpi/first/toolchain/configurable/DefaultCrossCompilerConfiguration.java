package edu.wpi.first.toolchain.configurable;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import edu.wpi.first.toolchain.ToolchainDescriptorBase;

public class DefaultCrossCompilerConfiguration implements CrossCompilerConfiguration {
  private final String name;
  private String architecture;
  private String operatingSystem;
  private String compilerPrefix;
  private Property<Boolean> optional;
  private ToolchainDescriptorBase descriptor;

  @Inject
  public DefaultCrossCompilerConfiguration(String name, ObjectFactory factory) {
    this.name = name;
    optional = factory.property(Boolean.class);
  }

  public DefaultCrossCompilerConfiguration(String name, ToolchainDescriptorBase descriptor, Property<Boolean> optional) {
    this.name = name;
    this.descriptor = descriptor;
    this.optional = optional;
  }

  public String getName() {
    return name;
  }

  /**
   * @return the architecture
   */
  public String getArchitecture() {
    return architecture;
  }

  /**
   * @param architecture the architecture to set
   */
  public void setArchitecture(String architecture) {
    this.architecture = architecture;
  }

  /**
   * @return the operatingSystem
   */
  public String getOperatingSystem() {
    return operatingSystem;
  }

  /**
   * @param operatingSystem the operatingSystem to set
   */
  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  /**
   * @return the compilerPrefix
   */
  public String getCompilerPrefix() {
    return compilerPrefix;
  }

  /**
   * @param compilerPrefix the compilerPrefix to set
   */
  public void setCompilerPrefix(String compilerPrefix) {
    this.compilerPrefix = compilerPrefix;
  }

  @Override
  public Property<Boolean> getOptional() {
    return optional;
  }

  @Override
  public void setToolchainDescriptor(ToolchainDescriptorBase optional) {
    this.descriptor = optional;
  }

  @Override
  public ToolchainDescriptorBase getToolchainDescriptor() {
    return descriptor;
  }
}
