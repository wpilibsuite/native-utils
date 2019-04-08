package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named
import groovy.transform.CompileStatic

@Managed
@CompileStatic
interface BuildConfig extends Named {
    @SuppressWarnings("GroovyUnusedDeclaration")
    void setArchitecture(String arch)

    String getArchitecture()

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
    void setCCompilerArgs(List<String> args)

    List<String> getCCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugCCompilerArgs(List<String> args)

    List<String> getDebugCCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseCCompilerArgs(List<String> args)

    List<String> getReleaseCCompilerArgs()


    @SuppressWarnings("GroovyUnusedDeclaration")
    void setObjCCompilerArgs(List<String> args)

    List<String> getObjCCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugObjCCompilerArgs(List<String> args)

    List<String> getDebugObjCCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseObjCCompilerArgs(List<String> args)

    List<String> getReleaseObjCCompilerArgs()


    @SuppressWarnings("GroovyUnusedDeclaration")
    void setObjCppCompilerArgs(List<String> args)

    List<String> getObjCppCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugObjCppCompilerArgs(List<String> args)

    List<String> getDebugObjCppCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseObjCppCompilerArgs(List<String> args)

    List<String> getReleaseObjCppCompilerArgs()


    @SuppressWarnings("GroovyUnusedDeclaration")
    void setAsmCompilerArgs(List<String> args)

    List<String> getAsmCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setDebugAsmCompilerArgs(List<String> args)

    List<String> getDebugAsmCompilerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseAsmCompilerArgs(List<String> args)

    List<String> getReleaseAsmCompilerArgs()


    @SuppressWarnings("GroovyUnusedDeclaration")
    void setReleaseLinkerArgs(List<String> args)

    List<String> getReleaseLinkerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setLinkerArgs(List<String> args)

    List<String> getLinkerArgs()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setExclude(List<String> toExclude)

    List<String> getExclude()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setInclude(List<String> toInclude)

    List<String> getInclude()

    @SuppressWarnings("GroovyUnusedDeclaration")
    void setSkipTests(boolean skip)

    boolean getSkipTests()

    void setStripBuildTypes(List<String> buildTypes)

    List<String> getStripBuildTypes()
}
