package edu.wpi.first.deployutils.deploy.target.discovery;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import edu.wpi.first.deployutils.deploy.StorageService;

public interface TargetDiscoveryWorkerParameters extends WorkParameters {
    Property<StorageService> getStorageService();
    Property<Integer> getIndex();
}
