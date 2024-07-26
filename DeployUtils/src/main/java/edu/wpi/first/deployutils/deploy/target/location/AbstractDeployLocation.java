package edu.wpi.first.deployutils.deploy.target.location;

import javax.inject.Inject;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public abstract class AbstractDeployLocation implements DeployLocation {
    private final RemoteTarget target;
    private final String name;

    @Inject
    public AbstractDeployLocation(String name, RemoteTarget target) {
        this.name = name;
        this.target = target;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RemoteTarget getTarget() {
        return this.target;
    }
}
