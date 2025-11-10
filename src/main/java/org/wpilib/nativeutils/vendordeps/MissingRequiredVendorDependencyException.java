package org.wpilib.nativeutils.vendordeps;

public class MissingRequiredVendorDependencyException extends RuntimeException {
    public MissingRequiredVendorDependencyException(String requestingUuid, String requiredUuid, String errorMessage) {
        super("Missing Vendor Dependency " + requiredUuid + " for uuid " + requestingUuid + ". Reason: "
                + errorMessage);
    }
}
