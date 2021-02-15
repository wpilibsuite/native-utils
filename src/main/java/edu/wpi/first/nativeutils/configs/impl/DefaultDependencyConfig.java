package edu.wpi.first.nativeutils.configs.impl;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import edu.wpi.first.nativeutils.configs.DependencyConfig;

public class DefaultDependencyConfig implements DependencyConfig {
  private String groupId;
  private String artifactId;
  private String headerClassifier;
  private String sourceClassifier;
  private boolean skipMissingPlatform = false;
  private boolean skipCombinedDependency = false;

  private String ext;
  private String version;

  private boolean sharedUsedAtRuntime = true;
  private Set<String> staticPlatforms = new HashSet<>();
  private Set<String> sharedPlatforms = new HashSet<>();
  private Set<String> sharedExcludes = new HashSet<>();

  private String name;

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
   * @return the sharedUsedAtRuntime
   */
  public boolean getSharedUsedAtRuntime() {
    return sharedUsedAtRuntime;
  }

  /**
   * @param sharedUsedAtRuntime the sharedUsedAtRuntime to set
   */
  public void setSharedUsedAtRuntime(boolean sharedUsedAtRuntime) {
    this.sharedUsedAtRuntime = sharedUsedAtRuntime;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Set<String> getSharedPlatforms() {
    return sharedPlatforms;
  }

  @Override
  public Set<String> getStaticPlatforms() {
    return staticPlatforms;
  }

  @Override
  public Set<String> getSharedExcludes() {
    return sharedExcludes;
  }

  @Override
  public boolean isSkipMissingPlatform() {
    return skipMissingPlatform;
  }

  @Override
  public void setSkipMissingPlatform(boolean skipMissingPlatform) {
    this.skipMissingPlatform = skipMissingPlatform;
  }

  @Override
  public boolean isSkipCombinedDependency() {
    return skipCombinedDependency;
  }

  @Override
  public void setSkipCombinedDependency(boolean skipCombinedDependency) {
    this.skipCombinedDependency = skipCombinedDependency;
  }
}
