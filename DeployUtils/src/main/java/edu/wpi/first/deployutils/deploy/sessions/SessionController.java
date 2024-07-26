package edu.wpi.first.deployutils.deploy.sessions;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import edu.wpi.first.deployutils.deploy.CommandDeployResult;

public interface SessionController extends AutoCloseable {
    void open();

    CommandDeployResult execute(String command);

    void put(Map<String, File> files);

    void put(InputStream source, String dest);

    String friendlyString();
}
