package edu.wpi.first.nativeutils.configs.impl;

import javax.inject.Inject;

import edu.wpi.first.nativeutils.configs.ConfigurableCrossPlatformConfig;

public class DefaultConfigurableCrossPlatformConfig implements ConfigurableCrossPlatformConfig {
  private String name;
  private String architecture;
  private String operatingSystem;
  private String compilerPrefix;
  private boolean optional;

  @Inject
  public DefaultConfigurableCrossPlatformConfig(String name) {
    this.name = name;
  }

  /**
   * @return the name
   */
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
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  @Override
  public boolean getOptional() {
    return optional;
  }


}
