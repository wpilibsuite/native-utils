package edu.wpi.first.nativeutils.vendordeps;

import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

public class InvalidVendorDepYearException extends RuntimeException {
    public InvalidVendorDepYearException(JsonDependency dependency, String requiredYear) {
        super(String.format("Vendor Dependency %s has invalid year %s. Expected to be %s", dependency.name,
                dependency.frcYear, requiredYear));
    }
}
