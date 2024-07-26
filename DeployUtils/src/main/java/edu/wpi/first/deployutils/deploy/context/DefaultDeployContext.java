package edu.wpi.first.deployutils.deploy.context;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import edu.wpi.first.deployutils.PathUtils;
import edu.wpi.first.deployutils.deploy.CommandDeployResult;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.sessions.SessionController;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;
import edu.wpi.first.deployutils.log.ETLogger;

public class DefaultDeployContext implements DeployContext {

    private final SessionController session;
    private final ETLogger logger;
    private final DeployLocation deployLocation;
    private final String workingDir;

    @Inject
    public DefaultDeployContext(SessionController session, ETLogger logger, DeployLocation deployLocation,
            String workingDir) {
        this.session = session;
        this.logger = logger;
        this.deployLocation = deployLocation;
        this.workingDir = workingDir;
    }

    @Override
    public String getWorkingDir() {
        return workingDir;
    }

    @Override
    public DeployLocation getDeployLocation() {
        return deployLocation;
    }

    @Override
    public ETLogger getLogger() {
        return logger;
    }

    @Override
    public SessionController getController() {
        return session;
    }

    @Override
    public CommandDeployResult execute(String command) {
        session.execute("mkdir -p " + workingDir);

        logger.log("  -C-> " + command + " @ " + workingDir);
        CommandDeployResult result = session.execute(String.join("\n", "cd " + workingDir, command));
        if (result != null) {
            if (result.getResult() != null && result.getResult().length() > 0) {
                logger.log("    -[" + result.getExitCode() + "]-> " + result.getResult());
            } else if (result.getExitCode() != 0) {
                logger.log("    -[" + result.getExitCode() + "]");
            }
        }
        return result;
    }

    @Override
    public void put(Map<String, File> files, CacheMethod cache) {
        session.execute("mkdir -p " + workingDir);

        Map<String, File> cacheHit = new HashMap<>();
        Map<String, File> cacheMiss = new HashMap<>(files);

        if (cache != null && cache.compatible(this)) {
            Set<String> updateRequired = cache.needsUpdate(this, files);
            for (String string : files.keySet()) {
                if (updateRequired.contains(string)) continue;
                cacheHit.put(string, files.get(string));
            }
            for (String string : cacheHit.keySet()) {
                cacheMiss.remove(string);
            }
        }

        if (!cacheMiss.isEmpty()) {
            Map<String, File> entries = cacheMiss.entrySet().stream().map(x -> {
                logger.log("  -F-> " + x.getValue() + " -> " + x.getKey() + " @ " + workingDir);
                return x;
            }).collect(Collectors.toMap(x -> PathUtils.combine(workingDir, x.getKey()), x -> x.getValue()));
            session.put(entries);
        }

        if (cacheHit.size() > 0) {
            logger.log("  " + cacheHit.size() + " file(s) are up-to-date and were not deployed");
        }
    }

    @Override
    public void put(File source, String dest, CacheMethod cache) {
        put(Map.of(dest, source), cache);
    }



    @Override
    public void put(Set<File> files, CacheMethod cache) {
        put(files.stream().collect(Collectors.toMap(x -> x.getName(), x -> x)), cache);
    }

    @Override
    public String friendlyString() {
        return session.friendlyString();
    }

    @Override
    public DeployContext subContext(String workingDir) {
        return new DefaultDeployContext(session, logger.push(), deployLocation, PathUtils.combine(this.workingDir, workingDir));
    }

    @Override
    public void put(InputStream source, String dest) {
        session.put(source, dest);
    }
}
