package edu.wpi.first.nativeutils.platforms;

import org.gradle.api.Named;
import org.gradle.api.provider.Property;

public interface PlatformConfig extends Named {
  Property<String> getPlatformPath();

  CompilerArgsConfig getCppCompiler();

  CompilerArgsConfig getLinker();

  CompilerArgsConfig getcCompiler();

  CompilerArgsConfig getAssembler();

  CompilerArgsConfig getObjcppCompiler();

  CompilerArgsConfig getObjcCompiler();
}
