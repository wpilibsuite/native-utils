package edu.wpi.first.nativeutils.configs.impl;

import javax.inject.Inject;

import edu.wpi.first.nativeutils.configs.ExportsConfig;

public abstract class DefaultExportsConfig implements ExportsConfig{

    private final String name;

    @Inject
    public DefaultExportsConfig(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
