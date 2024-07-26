package edu.wpi.first.deployutils.deploy.target.discovery.action;

import java.util.concurrent.Callable;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.discovery.DiscoveryFailedException;
import edu.wpi.first.deployutils.deploy.target.discovery.DiscoveryState;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;

public interface DiscoveryAction extends Callable<DeployContext> {
    DeployContext discover();

    DiscoveryFailedException getException();

    DiscoveryState getState();

    DeployLocation getDeployLocation();
}
