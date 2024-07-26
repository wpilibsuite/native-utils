package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.provider.Property;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class MavenArtifact extends FileArtifact {

    private final Property<Dependency> dependency;
    private final Property<Configuration> configuration;

    @Inject
    public MavenArtifact(String name, RemoteTarget target) {
        super(name, target);

        dependency = target.getProject().getObjects().property(Dependency.class);
        configuration = target.getProject().getObjects().property(Configuration.class);

        getPreWorkerThread().add(cfg -> {
            if (!configuration.isPresent() || !dependency.isPresent()) {
                return;
            }
            getFile().set(configuration.get().getSingleFile());
        });
    }

    public Property<Dependency> getDependency() {
        return dependency;
    }

    public Property<Configuration> getConfiguration() {
        return configuration;
    }
}
