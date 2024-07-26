package edu.wpi.first.deployutils.deploy.artifact;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;

public abstract class AbstractArtifact implements Artifact {
    private final String name;
    private final RemoteTarget target;
    private final TaskProvider<ArtifactDeployTask> deployTask;
    private final TaskProvider<ArtifactDeployTask> standaloneDeployTask;

    private boolean disabled = false;

    private final Property<String> directory;
    private final List<Action<DeployContext>> predeploy = new WrappedArrayList<>();
    private final List<Action<DeployContext>> postdeploy = new WrappedArrayList<>();
    private final List<Action<Artifact>> preWorkerThread = new WrappedArrayList<>();
    private Predicate<DeployContext> onlyIf = null;

    @Inject
    public AbstractArtifact(String name, RemoteTarget target) {
        this.name = name;
        directory = target.getProject().getObjects().property(String.class);
        directory.set("");
        this.target = target;

        deployTask = target.getProject().getTasks().register("deploy" + name + target.getName(), ArtifactDeployTask.class, task -> {
            task.getArtifact().set(this);
            task.getTarget().set(target);
            task.setGroup("DeployUtils");
            task.setDescription("Deploys " + name + " to " + target.getName());

            task.dependsOn(target.getTargetDiscoveryTask());
            task.getStorageService().set(target.getStorageServiceProvider());
            task.usesService(target.getStorageServiceProvider());
        });
        target.getDeployTask().configure(x -> x.dependsOn(deployTask));

        standaloneDeployTask = target.getProject().getTasks().register("deployStandalone" + name + target.getName(), ArtifactDeployTask.class, task -> {
            task.getArtifact().set(this);
            task.getTarget().set(target);
            task.setGroup("DeployUtils");
            task.setDescription("Deploys " + name + " to " + target.getName() + " as Standalone");

            task.dependsOn(target.getTargetDiscoveryTask());
            task.getStorageService().set(target.getStorageServiceProvider());
            task.usesService(target.getStorageServiceProvider());
        });
    }

    @Override
    public TaskProvider<ArtifactDeployTask> getStandaloneDeployTask() {
        return deployTask;
    }

    @Override
    public void allowStandaloneDeploy() {
        target.getStandaloneDeployTask().configure(x -> x.dependsOn(standaloneDeployTask));
    }

    @Override
    public TaskProvider<ArtifactDeployTask> getDeployTask() {
        return deployTask;
    }

    @Override
    public RemoteTarget getTarget() {
        return target;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void dependsOn(Object... paths) {
        dependsOnForDeployTask(paths);
        dependsOnForStandaloneDeployTask(paths);
    }

    @Override
    public void dependsOnForDeployTask(Object... paths) {
        deployTask.configure(y -> y.dependsOn(paths));
    }

    @Override
    public void dependsOnForStandaloneDeployTask(Object... paths) {
        deployTask.configure(y -> y.dependsOn(paths));
    }

    @Override
    public List<Action<Artifact>> getPreWorkerThread() {
        return preWorkerThread;
    }

    @Override
    public Property<String> getDirectory() {
        return directory;
    }

    @Override
    public List<Action<DeployContext>> getPredeploy() {
        return predeploy;
    }

    @Override
    public List<Action<DeployContext>> getPostdeploy() {
        return postdeploy;
    }

    public Predicate<DeployContext> getOnlyIf() {
        return onlyIf;
    }

    @Override
    public void setOnlyIf(Predicate<DeployContext> action) {
        onlyIf = action;
    }

    @Override
    public boolean isEnabled(DeployContext context) {
        if (disabled) return false;
        if (onlyIf == null) return true;
        if (onlyIf.test(context)) return true;
        if (context != null) {
            DeployLocation loc = context.getDeployLocation();
            if (loc != null) {
                RemoteTarget target = loc.getTarget();
                if (target != null) {
                    return target.isDry();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled() {
        setDisabled(true);
    }

    public void setDisabled(boolean state) {
        this.disabled = state;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.name + "]";
    }
}
