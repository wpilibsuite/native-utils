package edu.wpi.first.nativeutils.configs.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinedLibraryDependencySet extends BaseLibraryDependencySet {
    private final List<String> libs;
    private final Set<String> platforms;
    private final Set<String> flavors;
    private final Set<String> buildTypes;

    public CombinedLibraryDependencySet(String name, List<String> libs, Set<String> platforms, Set<String> flavors,
    Set<String> buildTypes) {
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
    public Set<String> getFlavors() {
        return flavors;
    }

    @Override
    public Set<String> getBuildTypes() {
        return buildTypes;
    }

    @Override
    public Set<String> getTargetPlatforms() {
        return platforms;
    }
}
