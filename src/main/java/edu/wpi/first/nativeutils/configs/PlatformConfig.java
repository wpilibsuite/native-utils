package edu.wpi.first.nativeutils.configs;

import org.gradle.api.Named;

public interface PlatformConfig extends Named {
  void setPlatformPath(String platformPath);
  String getPlatformPath();

  CompilerArgsConfig getCppCompiler();
  CompilerArgsConfig getLinker();
  CompilerArgsConfig getcCompiler();
  CompilerArgsConfig getAssembler();
  CompilerArgsConfig getObjcppCompiler();
  CompilerArgsConfig getObjcCompiler();
}
