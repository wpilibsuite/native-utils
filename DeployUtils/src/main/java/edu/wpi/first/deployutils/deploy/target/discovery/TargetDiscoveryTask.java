package edu.wpi.first.deployutils.deploy.target.discovery;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import edu.wpi.first.deployutils.deploy.StorageService;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public abstract class TargetDiscoveryTask extends DefaultTask implements Consumer<DeployContext> {

    @Internal
    public abstract Property<StorageService> getStorageService();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    private DeployContext activeContext;

    private RemoteTarget target;

    public void setTarget(RemoteTarget target) {
        this.target = target;
    }

    @Input
    public RemoteTarget getTarget() {
        return target;
    }

    @Internal
    public boolean isAvailable() {
        return activeContext != null;
    }

    @Internal
    public DeployContext getActiveContext() {
        if (activeContext != null) {
            return activeContext;
        } else {
            throw new GradleException("Target " + target.getName() + " is not available");
        }
    }

    @Override
    public void accept(DeployContext ctx) {
        this.activeContext = ctx;
    }

    @TaskAction
    public void discoverTarget() {
        StorageService storageService = getStorageService().get();
        ETLogger log = ETLoggerFactory.INSTANCE.create("TargetDiscoveryTask[" + target.getName() + "]");

        log.log("Discovering Target " + target.getName());
        int hashcode = storageService.submitDiscoveryStorage(target, this);

        // We use the Worker API since it allows for multiple of this task to run at the
        // same time. Inside the worker we split off into a threadpool so we can introduce
        // our own timeout logic.
        log.debug("Submitting worker ${hashcode}...");
        getWorkerExecutor().noIsolation().submit(TargetDiscoveryWorker.class, config -> {
            config.getStorageService().set(getStorageService());
            config.getIndex().set(hashcode);
        });
        log.debug("Submitted!");
    }
}
