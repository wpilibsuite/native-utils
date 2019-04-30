package edu.wpi.first.nativeutils.configs.impl;

import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;

import edu.wpi.first.nativeutils.configs.ExportsConfig;

public class DefaultExportsConfig implements ExportsConfig {
  private List<String> x86ExcludeSymbols;
  private List<String> x64ExcludeSymbols;
  private List<String> excludeBuildTypes;
  private Action<List<String>> x86SymbolFilter;
  private Action<List<String>> x64SymbolFilter;
  private String name;

  @Inject
  public DefaultExportsConfig(String name) {
    this.name = name;
  }

  /**
   * @return the x86ExcludeSymbols
   */
  public List<String> getX86ExcludeSymbols() {
    return x86ExcludeSymbols;
  }

  /**
   * @param x86ExcludeSymbols the x86ExcludeSymbols to set
   */
  public void setX86ExcludeSymbols(List<String> x86ExcludeSymbols) {
    this.x86ExcludeSymbols = x86ExcludeSymbols;
  }

  /**
   * @return the x64ExcludeSymbols
   */
  public List<String> getX64ExcludeSymbols() {
    return x64ExcludeSymbols;
  }

  /**
   * @param x64ExcludeSymbols the x64ExcludeSymbols to set
   */
  public void setX64ExcludeSymbols(List<String> x64ExcludeSymbols) {
    this.x64ExcludeSymbols = x64ExcludeSymbols;
  }

  /**
   * @return the excludeBuildTypes
   */
  public List<String> getExcludeBuildTypes() {
    return excludeBuildTypes;
  }

  /**
   * @param excludeBuildTypes the excludeBuildTypes to set
   */
  public void setExcludeBuildTypes(List<String> excludeBuildTypes) {
    this.excludeBuildTypes = excludeBuildTypes;
  }

  /**
   * @return the x86SymbolFilter
   */
  public Action<List<String>> getX86SymbolFilter() {
    return x86SymbolFilter;
  }

  /**
   * @param x86SymbolFilter the x86SymbolFilter to set
   */
  public void setX86SymbolFilter(Action<List<String>> x86SymbolFilter) {
    this.x86SymbolFilter = x86SymbolFilter;
  }

  /**
   * @return the x64SymbolFilter
   */
  public Action<List<String>> getX64SymbolFilter() {
    return x64SymbolFilter;
  }

  /**
   * @param x64SymbolFilter the x64SymbolFilter to set
   */
  public void setX64SymbolFilter(Action<List<String>> x64SymbolFilter) {
    this.x64SymbolFilter = x64SymbolFilter;
  }

  @Override
  public String getName() {
    return name;
  }
}
