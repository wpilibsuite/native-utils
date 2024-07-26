package edu.wpi.first.deployutils.deploy.artifact;

import java.util.List;
import java.util.function.Predicate;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public interface Artifact extends Named {
    TaskProvider<ArtifactDeployTask> getDeployTask();

    TaskProvider<ArtifactDeployTask> getStandaloneDeployTask();

    void allowStandaloneDeploy();

    RemoteTarget getTarget();

    void dependsOn(Object... paths);

    void dependsOnForDeployTask(Object... paths);

    void dependsOnForStandaloneDeployTask(Object... paths);

    List<Action<Artifact>> getPreWorkerThread();

    Property<String> getDirectory();

    List<Action<DeployContext>> getPredeploy();

    List<Action<DeployContext>> getPostdeploy();

    void setOnlyIf(Predicate<DeployContext> action);

    boolean isEnabled(DeployContext context);

    boolean isDisabled();
    void setDisabled();

    void deploy(DeployContext context);

    public default ExtensionContainer getExtensionContainer() {
        if (this instanceof ExtensionAware) {
            return ((ExtensionAware)this).getExtensions();
        }
        return null;
    }
}
