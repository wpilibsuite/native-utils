package edu.wpi.first.deployutils.deploy.target.discovery;

import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;

public class DiscoveryFailedException extends Exception {
    private static final long serialVersionUID = -4031180517437465326L;

    private final DiscoveryAction action;

    public DiscoveryAction getAction() {
        return action;
    }

    public DiscoveryFailedException(DiscoveryAction action, Throwable cause) {
        super(cause);
        this.action = action;
    }
}
