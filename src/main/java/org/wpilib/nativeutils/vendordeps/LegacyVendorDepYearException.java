package org.wpilib.nativeutils.vendordeps;

import org.wpilib.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

public class LegacyVendorDepYearException extends RuntimeException {
    public LegacyVendorDepYearException(JsonDependency dependency) {
        super("Vendor Dependency " + dependency.name
                + " still uses legacy 'wpilibYear' ('" + dependency.getLegacyWpilibYear()
                + "') instead of 'wpilibVersion'. Attempting to modify an existing dependency will break at runtime, and will result in loss of support from the WPILib team.");
    }
}
