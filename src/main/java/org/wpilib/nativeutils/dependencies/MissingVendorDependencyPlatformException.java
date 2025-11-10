package org.wpilib.nativeutils.dependencies;

public class MissingVendorDependencyPlatformException extends RuntimeException {
    public MissingVendorDependencyPlatformException(String artifactName, String expectedPlatform) {
        super("Could not find required platform " + expectedPlatform + " for artifact " + artifactName);
    }
}
