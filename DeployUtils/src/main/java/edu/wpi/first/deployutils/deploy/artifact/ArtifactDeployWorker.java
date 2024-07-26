package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.workers.WorkAction;

import edu.wpi.first.deployutils.deploy.StorageService.DeployStorage;
import edu.wpi.first.deployutils.deploy.context.DeployContext;

public abstract class ArtifactDeployWorker implements WorkAction<ArtifactDeployParameters> {

    @Override
    public void execute() {
        Integer index = getParameters().getIndex().get();
        DeployStorage storage = getParameters().getStorageService().get().getDeployStorage(index);

        DeployContext rootContext = storage.context;
        Artifact artifact = storage.artifact;
        run(rootContext, artifact);
    }

    public void run(DeployContext rootContext, Artifact artifact) {
        DeployContext context = rootContext.subContext(artifact.getDirectory().get());
        boolean enabled = artifact.isEnabled(context);
        if (enabled) {
            ArtifactRunner.runDeploy(artifact, context);
        } else {
            context.getLogger().log("Artifact skipped");
        }
    }
}
