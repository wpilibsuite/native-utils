package edu.wpi.first.nativeutils.configs.internal;

import javax.inject.Inject;

public abstract class DefaultCombinedNativeLibraryConfig implements CombinedNativeLibraryConfig {
    private final String name;

    @Inject
    public DefaultCombinedNativeLibraryConfig(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
