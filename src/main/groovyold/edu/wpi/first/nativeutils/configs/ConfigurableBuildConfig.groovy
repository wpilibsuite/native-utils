package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named
import groovy.transform.CompileStatic

@Managed
@CompileStatic
interface ConfigurableBuildConfig extends BuildConfig {
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setCompilerFamily(String family)

    String getCompilerFamily()
}
