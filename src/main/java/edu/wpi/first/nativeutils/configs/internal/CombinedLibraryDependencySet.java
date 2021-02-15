package edu.wpi.first.nativeutils.configs.internal;

import java.util.List;

public class CombinedLibraryDependencySet extends BaseLibraryDependencySet {
    private final List<String> libs;
    private final List<String> platforms;
    private final List<String> flavors;
    private final List<String> buildTypes;

    public CombinedLibraryDependencySet(String name, List<String> libs, List<String> platforms, List<String> flavors,
            List<String> buildTypes) {
        super(name);
        this.libs = libs;
        this.platforms = platforms;
        this.flavors = flavors;
        this.buildTypes = buildTypes;
    }

    public List<String> getLibs() {
        return libs;
    }

    @Override
    public List<String> getFlavors() {
        return flavors;
    }

    @Override
    public List<String> getBuildTypes() {
        return buildTypes;
    }

    @Override
    public List<String> getTargetPlatforms() {
        return platforms;
    }
}
