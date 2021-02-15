package edu.wpi.first.nativeutils.configs.internal;

import java.util.List;
import java.util.Set;

import org.gradle.api.Named;

public interface BaseNativeLibraryConfig extends Named {
    // Overrides the 'named' status for this object.
    // This is primarily used for cases where there are more
    // than one version of a library, for different platforms
    void setLibraryName(String libraryName);
    String getLibraryName();

    void setTargetPlatform(String platforms);
    String getTargetPlatform();

    // Same as above, but for multiple platforms. Appends platform name
    // to the binary names
    void setTargetPlatforms(Set<String> platforms_multi);
    Set<String> getTargetPlatforms();

    void setFlavor(String flavour);
    String getFlavor();

    // Same as above, but for multiple flavors. Appends platform name
    // to the binary names
    void setFlavors(Set<String> flavors_multi);
    Set<String> getFlavors();

    void setBuildType(String type);
    String getBuildType();

    // Same as above, but for multiple build types. Appends platform name
    // to the binary names
    void setBuildTypes(Set<String> builds_multi);
    Set<String> getBuildTypes();

    boolean isSkipMissingPlatform();
    void setSkipMissingPlatform(boolean skip);
}
