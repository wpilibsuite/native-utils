package edu.wpi.first.deployutils.deploy.target.location;

import javax.inject.Inject;

import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DryDiscoveryAction;

public class DryDeployLocation extends AbstractDeployLocation {

    private DeployLocation inner;

    @Inject
    public DryDeployLocation(String name, DeployLocation inner) {
        super(name, inner.getTarget());
        this.inner = inner;
    }

    @Override
    public DiscoveryAction createAction() {
        return new DryDiscoveryAction(inner);
    }

    @Override
    public String friendlyString() {
        return "DryRun DeployLocation (wrapping " + inner.toString() + ")";
    }
}
