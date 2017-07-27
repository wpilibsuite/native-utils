package edu.wpi.first.nativeutils.configs

import org.gradle.model.*
import org.gradle.api.Named

@Managed
interface ExportsConfig extends Named {
    void setX86ExcludeSymbols(List<String> symbols)

    List<String> getX86ExcludeSymbols();

    void setX64ExcludeSymbols(List<String> symbols)

    List<String> getX64ExcludeSymbols();

    void setExcludeBuildTypes(List<String> excludes)

    List<String> getExcludeBuildTypes()

    @Unmanaged
    void setX86SymbolFilter(Closure closure)

    @Unmanaged
    Closure getX86SymbolFilter()

    @Unmanaged
    void setX64SymbolFilter(Closure closure)

    @Unmanaged
    Closure getX64SymbolFilter()
}