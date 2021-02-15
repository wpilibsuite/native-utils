package edu.wpi.first.nativeutils.configs.internal;

import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Named;

public abstract class BaseLibraryDependencySet implements Named {
    private final String name;

    @Inject
    public BaseLibraryDependencySet(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Set<String> getFlavors();
    public abstract Set<String> getBuildTypes();
    public abstract Set<String> getTargetPlatforms();


    public boolean appliesTo(String flavorName, String buildTypeName, String platformName) {
        Set<String> flavors = getFlavors();
        Set<String> targetPlatforms = getTargetPlatforms();
        Set<String> buildTypes = getBuildTypes();
        if (flavors != null && !flavors.isEmpty() && !flavors.contains(flavorName))
            return false;
        if (buildTypes != null && !buildTypes.isEmpty() && !buildTypes.contains(buildTypeName))
            return false;
        if (targetPlatforms != null && !targetPlatforms.isEmpty() && !targetPlatforms.contains(platformName))
            return false;

        return true;
    }
}
