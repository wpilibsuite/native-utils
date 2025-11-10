package edu.wpi.first.nativeutils.exports;

import javax.inject.Inject;

public abstract class DefaultExportsConfig implements ExportsConfig {

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
