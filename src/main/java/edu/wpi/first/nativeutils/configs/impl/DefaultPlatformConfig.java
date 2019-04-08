package edu.wpi.first.nativeutils.configs.impl;

import edu.wpi.first.nativeutils.configs.CompilerArgsConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class DefaultPlatformConfig implements PlatformConfig {

  private String name;

  public DefaultPlatformConfig(String name) {
    this.name = name;
  }

  private String platformPath = null;

  private CompilerArgsConfig cppCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig linker = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig cCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig assembler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig objCCompiler = new DefaultCompilerArgsConfig();
  private CompilerArgsConfig objCppCompiler = new DefaultCompilerArgsConfig();

  /**
   * @return the platformPath
   */
  public String getPlatformPath() {
    return platformPath;
  }

  /**
   * @param platformPath the platformPath to set
   */
  public void setPlatformPath(String platformPath) {
    this.platformPath = platformPath;
  }

  /**
   * @return the cppCompiler
   */
  public CompilerArgsConfig getCppCompiler() {
    return cppCompiler;
  }

  /**
   * @param cppCompiler the cppCompiler to set
   */
  public void setCppCompiler(CompilerArgsConfig cppCompiler) {
    this.cppCompiler = cppCompiler;
  }

  /**
   * @return the linker
   */
  public CompilerArgsConfig getLinker() {
    return linker;
  }

  /**
   * @param linker the linker to set
   */
  public void setLinker(CompilerArgsConfig linker) {
    this.linker = linker;
  }

  /**
   * @return the cCompiler
   */
  public CompilerArgsConfig getcCompiler() {
    return cCompiler;
  }

  /**
   * @param cCompiler the cCompiler to set
   */
  public void setcCompiler(CompilerArgsConfig cCompiler) {
    this.cCompiler = cCompiler;
  }

  /**
   * @return the assembler
   */
  public CompilerArgsConfig getAssembler() {
    return assembler;
  }

  /**
   * @param assembler the assembler to set
   */
  public void setAssembler(CompilerArgsConfig assembler) {
    this.assembler = assembler;
  }

  /**
   * @return the objCCompiler
   */
  public CompilerArgsConfig getObjcCompiler() {
    return objCCompiler;
  }

  /**
   * @param objCCompiler the objCCompiler to set
   */
  public void setObjcCompiler(CompilerArgsConfig objCCompiler) {
    this.objCCompiler = objCCompiler;
  }

  /**
   * @return the objCppCompiler
   */
  public CompilerArgsConfig getObjcppCompiler() {
    return objCppCompiler;
  }

  /**
   * @param objCppCompiler the objCppCompiler to set
   */
  public void setObjcppCompiler(CompilerArgsConfig objCppCompiler) {
    this.objCppCompiler = objCppCompiler;
  }

  @Override
  public String getName() {
    return name;
  }

}
