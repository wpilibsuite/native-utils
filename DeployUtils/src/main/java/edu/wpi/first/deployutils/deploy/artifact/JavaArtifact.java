package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class JavaArtifact extends FileArtifact {

    @Inject
    public JavaArtifact(String name, RemoteTarget target) {
        super(name, target);

        jarProvider = target.getProject().getObjects().property(Jar.class);

        dependsOn(jarProvider);
    }

    private final Property<Jar> jarProvider;

    public Provider<Jar> getJarProvider() {
        return jarProvider;
    }

    public void setJarTask(TaskProvider<Jar> jarTask) {
        jarProvider.set(jarTask);
        getFile().set(jarTask.get().getArchiveFile().map(x -> x.getAsFile()));
    }

    public void setJarTask(Jar jarTask) {
        jarProvider.set(jarTask);
        dependsOn(jarTask);
        getFile().set(jarTask.getArchiveFile().map(x -> x.getAsFile()));
    }
}
