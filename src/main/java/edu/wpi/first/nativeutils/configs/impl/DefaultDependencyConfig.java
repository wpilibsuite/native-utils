package edu.wpi.first.nativeutils.configs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import edu.wpi.first.nativeutils.configs.DependencyConfig;

public class DefaultDependencyConfig implements DependencyConfig {
  private String groupId;
  private String artifactId;
  private String headerClassifier;
  private String sourceClassifier;

  private String ext;
  private String version;
  private int sortOrder;
  private Map<String, List<String>> headerOnlyConfigs = new HashMap<>();
  private Map<String, List<String>> sharedConfigs = new HashMap<>();
  private Map<String, List<String>> staticConfigs = new HashMap<>();

  private boolean compileOnlyShared;
  private String name;

  private List<String> linkExcludes = new ArrayList<>();

  @Inject
  public DefaultDependencyConfig(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the artifactId
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * @param artifactId the artifactId to set
   */
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * @return the headerClassifier
   */
  public String getHeaderClassifier() {
    return headerClassifier;
  }

  /**
   * @param headerClassifier the headerClassifier to set
   */
  public void setHeaderClassifier(String headerClassifier) {
    this.headerClassifier = headerClassifier;
  }

  /**
   * @return the sourceClassifier
   */
  public String getSourceClassifier() {
    return sourceClassifier;
  }

  /**
   * @param sourceClassifier the sourceClassifier to set
   */
  public void setSourceClassifier(String sourceClassifier) {
    this.sourceClassifier = sourceClassifier;
  }

  /**
   * @return the ext
   */
  public String getExt() {
    return ext;
  }

  /**
   * @param ext the ext to set
   */
  public void setExt(String ext) {
    this.ext = ext;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the sortOrder
   */
  public int getSortOrder() {
    return sortOrder;
  }

  /**
   * @param sortOrder the sortOrder to set
   */
  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * @return the headerOnlyConfigs
   */
  public Map<String, List<String>> getHeaderOnlyConfigs() {
    return headerOnlyConfigs;
  }

  /**
   * @param headerOnlyConfigs the headerOnlyConfigs to set
   */
  public void setHeaderOnlyConfigs(Map<String, List<String>> headerOnlyConfigs) {
    this.headerOnlyConfigs = headerOnlyConfigs;
  }

  /**
   * @return the sharedConfigs
   */
  public Map<String, List<String>> getSharedConfigs() {
    return sharedConfigs;
  }

  /**
   * @param sharedConfigs the sharedConfigs to set
   */
  public void setSharedConfigs(Map<String, List<String>> sharedConfigs) {
    this.sharedConfigs = sharedConfigs;
  }

  /**
   * @return the staticConfigs
   */
  public Map<String, List<String>> getStaticConfigs() {
    return staticConfigs;
  }

  /**
   * @param staticConfigs the staticConfigs to set
   */
  public void setStaticConfigs(Map<String, List<String>> staticConfigs) {
    this.staticConfigs = staticConfigs;
  }

  /**
   * @return the compileOnlyShared
   */
  public boolean getCompileOnlyShared() {
    return compileOnlyShared;
  }

  /**
   * @param compileOnlyShared the compileOnlyShared to set
   */
  public void setCompileOnlyShared(boolean compileOnlyShared) {
    this.compileOnlyShared = compileOnlyShared;
  }

  /**
   * @return the linkExcludes
   */
  public List<String> getLinkExcludes() {
    return linkExcludes;
  }

  /**
   * @param linkExcludes the linkExcludes to set
   */
  public void setLinkExcludes(List<String> linkExcludes) {
    this.linkExcludes = linkExcludes;
  }
}
