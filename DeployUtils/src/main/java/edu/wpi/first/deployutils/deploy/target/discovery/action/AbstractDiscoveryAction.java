package edu.wpi.first.deployutils.deploy.target.discovery.action;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.discovery.DiscoveryFailedException;
import edu.wpi.first.deployutils.deploy.target.discovery.TargetVerificationException;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;

public abstract class AbstractDiscoveryAction implements DiscoveryAction {
    private final DeployLocation location;
    private DiscoveryFailedException ex = null;

    public AbstractDiscoveryAction(DeployLocation location) {
        this.location = location;
    }

    @Override
    public DeployLocation getDeployLocation() {
        return location;
    }

    @Override
    public DeployContext call() throws Exception {
        try {
            return discover();
        } catch (Throwable t) {
            DiscoveryFailedException e = new DiscoveryFailedException(this, t);
            if (!(t instanceof InterruptedException)) {
                ex = e;
            }
            throw e;
        }
    }

    @Override
    public DiscoveryFailedException getException() {
        return ex;
    }

    void verify(DeployContext ctx) {
        if (!location.getTarget().verify(ctx)) {
            throw new TargetVerificationException("Target failed verify (onlyIf) check!");
        }
    }
}
