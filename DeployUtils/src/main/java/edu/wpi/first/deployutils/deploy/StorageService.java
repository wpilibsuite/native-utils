package edu.wpi.first.deployutils.deploy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import edu.wpi.first.deployutils.deploy.artifact.Artifact;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.sessions.SessionController;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public abstract class StorageService implements BuildService<BuildServiceParameters.None>, AutoCloseable {

    public static class DeployStorage {
        public final DeployContext context;
        public final Artifact artifact;
        public DeployStorage(DeployContext context, Artifact artifact) {
            this.context = context;
            this.artifact = artifact;
        }
    }

    public static class DiscoveryStorage {
        public final RemoteTarget target;
        public DiscoveryStorage(RemoteTarget target, Consumer<DeployContext> contextSet) {
            this.target = target;
            this.contextSet = contextSet;
        }
        public final Consumer<DeployContext> contextSet;
    }

    private final AtomicInteger hashIndex;
    private final ConcurrentMap<Integer, DeployStorage> deployerStorage;
    private final ConcurrentMap<Integer, DiscoveryStorage> discoveryStorage;
    private final List<SessionController> sessions;

    @Inject
    public StorageService() {
        hashIndex = new AtomicInteger(0);
        deployerStorage = new ConcurrentHashMap<>();
        discoveryStorage = new ConcurrentHashMap<>();
        sessions = Collections.synchronizedList(new ArrayList<>());
    }

    public int submitDeployStorage(DeployContext context, Artifact artifact) {
        DeployStorage ds = new DeployStorage(context, artifact);
        int idx = hashIndex.getAndIncrement();
        deployerStorage.put(idx, ds);
        return idx;
    }

    public DeployStorage getDeployStorage(int idx) {
        return deployerStorage.get(idx);
    }

    public int submitDiscoveryStorage(RemoteTarget target, Consumer<DeployContext> cb) {
        DiscoveryStorage stg = new DiscoveryStorage(target, cb);
        int idx = hashIndex.getAndIncrement();
        discoveryStorage.put(idx, stg);
        return idx;
    }

    public DiscoveryStorage getDiscoveryStorage(int idx) {
        return discoveryStorage.get(idx);
    }

    public void addSessionForCleanup(SessionController session) {
        sessions.add(session);
    }

    @Override
    public void close() {
        deployerStorage.clear();
        discoveryStorage.clear();
        for (SessionController sessionController : sessions) {
            try {
                sessionController.close();
            } catch (Exception e) {
            }
        }
        sessions.clear();
    }
}
