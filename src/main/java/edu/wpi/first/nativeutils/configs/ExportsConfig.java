package edu.wpi.first.nativeutils.configs;

import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Named;

public interface ExportsConfig extends Named {
    void setX86ExcludeSymbols(List<String> symbols);

    List<String> getX86ExcludeSymbols();

    void setX64ExcludeSymbols(List<String> symbols);

    List<String> getX64ExcludeSymbols();

    void setExcludeBuildTypes(List<String> excludes);

    List<String> getExcludeBuildTypes();

    void setX86SymbolFilter(Action<List<String>> closure);

    Action<List<String>> getX86SymbolFilter();

    void setX64SymbolFilter(Action<List<String>> closure);

    Action<List<String>> getX64SymbolFilter();
}
