package edu.wpi.first.nativeutils.vendordeps;

import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

public class InvalidVendorDepYearException extends RuntimeException {
    public InvalidVendorDepYearException(JsonDependency dependency, String requiredYear) {
        super(String.format("Vendor Dependency %s has invalid year %s. Expected to be %s. Do not manually change a prior year dependency. This will not work, and the WPILib team will not offer support if you attempt this.", dependency.name,
                dependency.frcYear, requiredYear));
    }
}
