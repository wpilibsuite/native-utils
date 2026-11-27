package org.wpilib.nativeutils.vendordeps;

import org.wpilib.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

public class InvalidVendorDepVersionException extends RuntimeException {
    public InvalidVendorDepVersionException(JsonDependency dependency, String requiredVersion) {
        this(dependency, requiredVersion, null);
    }

    public InvalidVendorDepVersionException(JsonDependency dependency, String requiredVersion, Throwable cause) {
        super(message(dependency, requiredVersion, cause), cause);
    }

    private static String message(JsonDependency dependency, String requiredVersion, Throwable cause) {
        StringBuilder message = new StringBuilder()
                .append("Vendor Dependency ")
                .append(dependency.name)
                .append(" has incompatible wpilibVersion '")
                .append(dependency.wpilibVersion)
                .append("'. Expected a version compatible with '")
                .append(requiredVersion);
        if (cause instanceof InvalidWPILibVersionException) {
            message.append(" The vendordep value could not be parsed: ").append(cause.getMessage());
        }
        message.append(" Reach out to the vendor to get a new version of the dependency. Attempting to modify an existing dependency will break at runtime, and will result in loss of support from the WPILib team.");
        return message.toString();
    }
}
