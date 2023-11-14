package edu.wpi.first.nativeutils.vendordeps;

import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

public class InvalidVendorDepYearException extends RuntimeException {
    public InvalidVendorDepYearException(JsonDependency dependency, String requiredYear) {
        super(String.format("Vendor Dependency %s has invalid year %s. Expected to be %s. Reach out to the vendor to get a new version of the dependency. Attempting to modify an existing dependency will break at runtime, and will result in loss of support from the WPILib team.", dependency.name,
                dependency.frcYear, requiredYear));
    }
}
