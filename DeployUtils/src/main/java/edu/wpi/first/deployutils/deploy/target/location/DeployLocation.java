package edu.wpi.first.deployutils.deploy.target.location;

import org.gradle.api.Named;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;

public interface DeployLocation extends Named {
    DiscoveryAction createAction();

    RemoteTarget getTarget();

    String friendlyString();
}
