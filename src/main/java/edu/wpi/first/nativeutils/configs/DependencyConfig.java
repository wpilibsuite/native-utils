package edu.wpi.first.nativeutils.configs;

import java.util.List;
import java.util.Map;

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

    void setSortOrder(int sortOrder);

    int getSortOrder();

    void setHeaderOnlyConfigs(Map<String, List<String>> configs);

    Map<String, List<String>> getHeaderOnlyConfigs();

    void setSharedConfigs(Map<String, List<String>> configs);

    Map<String, List<String>> getSharedConfigs();

    void setStaticConfigs(Map<String, List<String>> configs);

    Map<String, List<String>> getStaticConfigs();

    void setCompileOnlyShared(boolean set);

    boolean getCompileOnlyShared();

    void setLinkExcludes(List<String> excludes);

    List<String> getLinkExcludes();
}
