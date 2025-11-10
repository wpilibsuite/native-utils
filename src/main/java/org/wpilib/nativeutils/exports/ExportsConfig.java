package org.wpilib.nativeutils.exports;

import java.io.File;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;

import groovy.lang.Closure;

public interface ExportsConfig extends Named {
    ListProperty<String> getArm64ExcludeSymbols();

    ListProperty<String> getX86ExcludeSymbols();

    ListProperty<String> getX64ExcludeSymbols();

    ListProperty<String> getExcludeBuildTypes();

    Property<Action<List<String>>> getArm64SymbolFilter();

    Property<Action<List<String>>> getX86SymbolFilter();

    Property<Action<List<String>>> getX64SymbolFilter();

    Property<Spec<File>> getObjectFilter();

    Property<Closure<?>> getObjectFilterClosure();
}
