package edu.wpi.first.nativeutils.configs;

import java.util.List;
import java.util.Set;

import org.gradle.api.Named;

public interface CombinedDependencyConfig extends Named {
  List<String> getDependencies();
  Set<String> getTargetPlatforms();
  String getLibraryName();
  void setLibraryName(String name);
}
