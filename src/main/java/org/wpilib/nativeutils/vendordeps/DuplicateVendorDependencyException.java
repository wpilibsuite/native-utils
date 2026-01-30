package org.wpilib.nativeutils.vendordeps;

public class DuplicateVendorDependencyException extends RuntimeException {
    public DuplicateVendorDependencyException(String firstVendordepFile, String secondVendordepFile, String uuid) {
        super("Duplicate Vendordeps detected. " + firstVendordepFile + " and "
                            + secondVendordepFile + " have the same UUID: " + uuid + ". Remove one of these vendordeps to avoid conflicts.");
    }
}
