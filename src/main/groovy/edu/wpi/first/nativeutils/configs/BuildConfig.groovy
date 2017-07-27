package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named

@Managed
interface BuildConfig extends Named {
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setArchitecture(String arch)

    String getArchitecture()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setToolChainPrefix(String prefix)

    String getToolChainPrefix()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setOperatingSystem(String os)

    String getOperatingSystem()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setCompilerArgs(List<String> args)

    List<String> getCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugCompilerArgs(List<String> args)

    List<String> getDebugCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugLinkerArgs(List<String> args)

    List<String> getDebugLinkerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseCompilerArgs(List<String> args)

    List<String> getReleaseCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseLinkerArgs(List<String> args)

    List<String> getReleaseLinkerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setLinkerArgs(List<String> args)

    List<String> getLinkerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setCompilerFamily(String family)

    String getCompilerFamily()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setExclude(List<String> toExclude)

    List<String> getExclude()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setInclude(List<String> toInclude)

    List<String> getInclude()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setSkipTests(boolean skip)

    boolean getSkipTests()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugStripBinaries(boolean strip)

    boolean getDebugStripBinaries()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseStripBinaries(boolean strip)

    boolean getReleaseStripBinaries()

    @Unmanaged
    void setDetectPlatform(Closure closure)
    @Unmanaged
    Closure getDetectPlatform()

}