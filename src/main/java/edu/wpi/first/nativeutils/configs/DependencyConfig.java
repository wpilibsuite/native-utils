package edu.wpi.first.nativeutils.configs;

import java.util.Set;

import org.gradle.api.Named;

public interface DependencyConfig extends Named {
    void setGroupId(String id);
    String getGroupId();

    void setArtifactId(String id);
    String getArtifactId();

    void setHeaderClassifier(String classifier);
    String getHeaderClassifier();

    void setSourceClassifier(String classifier);
    String getSourceClassifier();

    void setExt(String extension);
    String getExt();

    void setVersion(String version);
    String getVersion();

    void setSharedUsedAtRuntime(boolean usedAtRuntime);
    boolean getSharedUsedAtRuntime();

    Set<String> getSharedPlatforms();
    Set<String> getStaticPlatforms();

    Set<String> getSharedExcludes();

    boolean isSkipMissingPlatform();
    void setSkipMissingPlatform(boolean skipMissingPlatform);

    boolean isSkipCombinedDependency();
    void setSkipCombinedDependency(boolean skipCombinedDependency);
}
