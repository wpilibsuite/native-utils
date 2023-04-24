package edu.wpi.first.nativeutils.dependencies;

import org.gradle.api.file.FileCollection;

public class ResolvedNativeDependency {
  private final FileCollection includeRoots;
  private final FileCollection linkFiles;
  private final FileCollection runtimeFiles;
  private final FileCollection sourceRoots;

  public ResolvedNativeDependency(
      FileCollection includeRoots,
      FileCollection sourceRoots,
      FileCollection linkFiles,
      FileCollection runtimeFiles) {
    this.includeRoots = includeRoots;
    this.linkFiles = linkFiles;
    this.runtimeFiles = runtimeFiles;
    this.sourceRoots = sourceRoots;
  }

  public FileCollection getIncludeRoots() {
    return includeRoots;
  }

  public FileCollection getLinkFiles() {
    return linkFiles;
  }

  public FileCollection getRuntimeFiles() {
    return runtimeFiles;
  }

  public FileCollection getSourceRoots() {
    return sourceRoots;
  }
}
