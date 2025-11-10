package org.wpilib.nativeutils.vendordeps;

public class ConflictingVendorDependencyException extends RuntimeException {
    public ConflictingVendorDependencyException(String requestingUuid, String requiredUuid, String errorMessage) {
        super("Conflicting Vendor Dependency " + requiredUuid + " for uuid " + requestingUuid + ". Reason: "
                + errorMessage);
    }
}
