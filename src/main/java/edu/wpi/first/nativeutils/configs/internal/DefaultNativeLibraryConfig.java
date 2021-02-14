package edu.wpi.first.nativeutils.configs.internal;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryConfig implements NativeLibraryConfig {
    private final String name;

    @Inject
    public DefaultNativeLibraryConfig(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
