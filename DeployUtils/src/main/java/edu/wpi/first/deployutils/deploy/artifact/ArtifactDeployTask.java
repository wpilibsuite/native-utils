package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import edu.wpi.first.deployutils.deploy.StorageService;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public abstract class ArtifactDeployTask extends DefaultTask {

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @Input
    public abstract Property<RemoteTarget> getTarget();
    @Input
    public abstract Property<Artifact> getArtifact();

    @Internal
    public abstract Property<StorageService> getStorageService();

    @TaskAction
    public void deployArtifact() {
        Logger log = Logging.getLogger(toString());
        Artifact artifact = getArtifact().get();
        RemoteTarget target = getTarget().get();
        StorageService storageService = getStorageService().get();

        log.debug("Deploying artifact " + artifact.getName() + " for target " + target.getName());

        for (Action<Artifact> toExecute : artifact.getPreWorkerThread()) {
            toExecute.execute(artifact);
        }

        DeployContext ctx = target.getTargetDiscoveryTask().get().getActiveContext();
        int index = storageService.submitDeployStorage(ctx, artifact);
        getWorkerExecutor().noIsolation().submit(ArtifactDeployWorker.class, config -> {
            config.getStorageService().set(getStorageService());
            config.getIndex().set(index);
        });
        log.debug("Workers submitted...");
    }

}
