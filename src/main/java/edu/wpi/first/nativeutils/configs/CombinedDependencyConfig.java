package edu.wpi.first.nativeutils.configs;

import java.util.List;

import org.gradle.api.Named;

public interface CombinedDependencyConfig extends Named {
  List<String> getDependencies();
  List<String> getTargetPlatforms();
  String getLibraryName();
  void setLibraryName(String name);
}
