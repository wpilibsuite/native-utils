package edu.wpi.first.deployutils.deploy.context;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import edu.wpi.first.deployutils.deploy.CommandDeployResult;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.sessions.SessionController;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;
import edu.wpi.first.deployutils.log.ETLogger;

public interface DeployContext {
    SessionController getController();

    ETLogger getLogger();

    String getWorkingDir();

    DeployLocation getDeployLocation();

    CommandDeployResult execute(String command);

    // Send a batch of files
    void put(Map<String, File> files, CacheMethod cache);

    // Send a single file
    void put(File source, String dest, CacheMethod cache);

    // Send multiple files, and trigger cache checking only once
    void put(Set<File> files, CacheMethod cache);

    // Put an input stream, with no caching
    void put(InputStream source, String dest);

    String friendlyString();

    DeployContext subContext(String workingDir);
}
