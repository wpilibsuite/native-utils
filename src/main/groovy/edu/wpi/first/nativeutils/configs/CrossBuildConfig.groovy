package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named

@Managed
interface CrossBuildConfig extends BuildConfig {
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setToolChainPath(String path)

    String getToolChainPath()

    void setSkipByDefault(boolean skip)

    boolean getSkipByDefault()
}