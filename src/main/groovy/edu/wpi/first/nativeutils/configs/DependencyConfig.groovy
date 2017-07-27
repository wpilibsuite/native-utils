package edu.wpi.first.nativeutils.configs

import org.gradle.model.*

@Managed
interface DependencyConfig {
    void setGroupId(String id)

    String getGroupId()

    void setArtifactId(String id)

    String getArtifactId()

    void setHeaderClassifier(String classifier)

    String getHeaderClassifier()

    void setExt(String extension)

    String getExt()

    void setVersion(String version)

    String getVersion()

    void setSortOrder(int sortOrder)

    int getSortOrder()

    @Unmanaged
    void setSharedConfigs(Map<String, List<String>> configs)

    @Unmanaged
    Map<String, List<String>> getSharedConfigs()

    @Unmanaged
    void setStaticConfigs(Map<String, List<String>> configs)

    @Unmanaged
    Map<String, List<String>> getStaticConfigs()
}