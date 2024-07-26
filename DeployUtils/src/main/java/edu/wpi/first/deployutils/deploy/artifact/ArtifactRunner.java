package edu.wpi.first.deployutils.deploy.artifact;

import java.util.List;

import org.gradle.api.Action;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

public class ArtifactRunner {
    public static void runDeploy(Artifact artifact, DeployContext context) {
        List<Action<DeployContext>> predeploy = artifact.getPredeploy();
        if (predeploy != null) {
            for (Action<DeployContext> action : predeploy) {
                action.execute(context);
            }
        }
        artifact.deploy(context);
        List<Action<DeployContext>> postdeploy = artifact.getPostdeploy();
        if (postdeploy != null) {
            for (Action<DeployContext> action : postdeploy) {
                action.execute(context);
            }
        }
    }
}
