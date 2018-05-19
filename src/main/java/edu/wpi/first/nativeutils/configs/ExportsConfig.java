package edu.wpi.first.nativeutils.configs;

import org.gradle.model.*;

import groovy.lang.Closure;

import java.util.List;

import org.gradle.api.Named;

@Managed
public interface ExportsConfig extends Named {
    void setX86ExcludeSymbols(List<String> symbols);

    List<String> getX86ExcludeSymbols();

    void setX64ExcludeSymbols(List<String> symbols);

    List<String> getX64ExcludeSymbols();

    void setExcludeBuildTypes(List<String> excludes);

    List<String> getExcludeBuildTypes();

    @Unmanaged
    void setX86SymbolFilter(Closure<List<String>> closure);

    @Unmanaged
    Closure<List<String>> getX86SymbolFilter();

    @Unmanaged
    void setX64SymbolFilter(Closure<List<String>> closure);

    @Unmanaged
    Closure<List<String>> getX64SymbolFilter();
}
