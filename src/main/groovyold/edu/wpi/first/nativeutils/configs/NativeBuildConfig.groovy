package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named
import groovy.transform.CompileStatic

@Managed
@CompileStatic
interface NativeBuildConfig extends ConfigurableBuildConfig {
    @Unmanaged
    void setDetectPlatform(Closure<Boolean> closure)

    @Unmanaged
    Closure<Boolean> getDetectPlatform()
}
