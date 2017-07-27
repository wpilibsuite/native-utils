package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named
import org.gradle.api.tasks.SourceSet

@Managed
interface JNIConfig extends Named {
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setJniDefinitionClasses(List<String> classes)

    List<String> getJniDefinitionClasses()

    @Unmanaged
    void setJniArmHeaderLocations(Map<String, File> locations)

    @Unmanaged
    Map<String, File> getJniArmHeaderLocations()

    @Unmanaged
    void setSourceSets(List<SourceSet> sources)

    @Unmanaged
    List<SourceSet> getSourceSets()

    void setSkipSymbolCheck(boolean skip)

    boolean getSkipSymbolCheck()
}