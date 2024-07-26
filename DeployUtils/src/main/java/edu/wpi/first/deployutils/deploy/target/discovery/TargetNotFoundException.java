package edu.wpi.first.deployutils.deploy.target.discovery;

public class TargetNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -4355670496129015011L;

    public TargetNotFoundException(String message) {
        super(message);
    }
}
