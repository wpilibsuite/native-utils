package edu.wpi.first.deployutils.deploy.cache;

import javax.inject.Inject;

public abstract class AbstractCacheMethod implements CacheMethod {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Inject
    public AbstractCacheMethod(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
