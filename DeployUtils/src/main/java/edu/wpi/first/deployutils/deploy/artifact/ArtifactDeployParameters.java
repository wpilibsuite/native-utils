package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import edu.wpi.first.deployutils.deploy.StorageService;

public interface ArtifactDeployParameters extends WorkParameters {
    Property<StorageService> getStorageService();
    Property<Integer> getIndex();
}
