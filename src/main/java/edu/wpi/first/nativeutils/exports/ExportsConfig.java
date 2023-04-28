package edu.wpi.first.nativeutils.exports;

import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public interface ExportsConfig extends Named {
    ListProperty<String> getArm64ExcludeSymbols();

    ListProperty<String> getX86ExcludeSymbols();

    ListProperty<String> getX64ExcludeSymbols();

    ListProperty<String> getExcludeBuildTypes();

    Property<Action<List<String>>> getArm64SymbolFilter();

    Property<Action<List<String>>> getX86SymbolFilter();

    Property<Action<List<String>>> getX64SymbolFilter();
}
