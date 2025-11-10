package org.wpilib.nativeutils.platforms;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class DefaultPlatformConfig implements PlatformConfig {

  private final String name;

  @Inject
  public DefaultPlatformConfig(String name, ObjectFactory objects) {
    this.name = name;
    this.platformPath = objects.property(String.class);
    this.platformPath.finalizeValueOnRead();
  }

  private final Property<String> platformPath;

  private CompilerArgsConfig cppCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig linker = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig cCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig assembler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig objCCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig objCppCompiler = new DefaultCompilerArgsConfig();

  /**
   * @return the platformPath
   */
  public Property<String> getPlatformPath() {
    return platformPath;
  }

  /**
   * @return the cppCompiler
   */
  public CompilerArgsConfig getCppCompiler() {
    return cppCompiler;
  }

  /**
   * @return the linker
   */
  public CompilerArgsConfig getLinker() {
    return linker;
  }

  /**
   * @return the cCompiler
   */
  public CompilerArgsConfig getcCompiler() {
    return cCompiler;
  }

  /**
   * @return the assembler
   */
  public CompilerArgsConfig getAssembler() {
    return assembler;
  }

  /**
   * @return the objCCompiler
   */
  public CompilerArgsConfig getObjcCompiler() {
    return objCCompiler;
  }

  /**
   * @return the objCppCompiler
   */
  public CompilerArgsConfig getObjcppCompiler() {
    return objCppCompiler;
  }

  @Override
  public String getName() {
    return name;
  }

}
