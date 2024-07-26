package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.api.provider.Property;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;

public interface CacheableArtifact extends Artifact {
    Property<CacheMethod> getCacheMethod();
}
