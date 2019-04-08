package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named
import groovy.transform.CompileStatic

@Managed
@CompileStatic
interface CrossBuildConfig extends ConfigurableBuildConfig {

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setToolChainPath(String path)

    String getToolChainPath()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setToolChainPrefix(String prefix)

    String getToolChainPrefix()

    void setSkipByDefault(boolean skip)

    boolean getSkipByDefault()
}
