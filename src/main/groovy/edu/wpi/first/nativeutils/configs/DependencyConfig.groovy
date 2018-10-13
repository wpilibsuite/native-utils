package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import groovy.transform.CompileStatic

@Managed
@CompileStatic
interface DependencyConfig {
    void setGroupId(String id)

    String getGroupId()

    void setArtifactId(String id)

    String getArtifactId()

    void setHeaderClassifier(String classifier)

    String getHeaderClassifier()

    void setSourceClassifier(String classifier)

    String getSourceClassifier()

    void setExt(String extension)

    String getExt()

    void setVersion(String version)

    String getVersion()

    void setSortOrder(int sortOrder)

    int getSortOrder()

    @Unmanaged
    void setHeaderOnlyConfigs(Map<String, List<String>> configs)

    @Unmanaged
    Map<String, List<String>> getHeaderOnlyConfigs()

    @Unmanaged
    void setSharedConfigs(Map<String, List<String>> configs)

    @Unmanaged
    Map<String, List<String>> getSharedConfigs()

    @Unmanaged
    void setStaticConfigs(Map<String, List<String>> configs)

    @Unmanaged
    Map<String, List<String>> getStaticConfigs()

    void setCompileOnlyShared(boolean set)

    boolean getCompileOnlyShared()

    @Unmanaged
    void setLinkExcludes(List<String> excludes)

    @Unmanaged
    List<String> getLinkExcludes()
}
