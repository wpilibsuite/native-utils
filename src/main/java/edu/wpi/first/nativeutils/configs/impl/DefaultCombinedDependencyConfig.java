package edu.wpi.first.nativeutils.configs.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.wpi.first.nativeutils.configs.CombinedDependencyConfig;

public class DefaultCombinedDependencyConfig implements CombinedDependencyConfig {
  private List<String> dependencies = new ArrayList<>();
  private List<String> targetPlatforms = new ArrayList<>();
  private String libraryName;

  private String name;

  @Inject
  public DefaultCombinedDependencyConfig(String name) {
    this.name = name;
    libraryName = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<String> getDependencies() {
    return dependencies;
  }

  @Override
  public List<String> getTargetPlatforms() {
    return targetPlatforms;
  }

  @Override
  public String getLibraryName() {
    return libraryName;
  }

  @Override
  public void setLibraryName(String name) {
    libraryName = name;
  }
}
