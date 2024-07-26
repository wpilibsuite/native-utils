package edu.wpi.first.deployutils.deploy.sessions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;

import edu.wpi.first.deployutils.deploy.CommandDeployResult;

public class DrySessionController extends AbstractSessionController implements IPSessionController {

    @Inject
    public DrySessionController() {
        super(1, null);
    }

    @Override
    public void open() {
        getLogger().info("DrySessionController opening");
    }

    @Override
    public CommandDeployResult execute(String command) {
        return new CommandDeployResult(command, "", 0);
    }

    @Override
    public void put(Map<String, File> files) { }

    @Override
    public String friendlyString() {
        return "DrySessionController";
    }

    @Override
    public void close() throws IOException {
        getLogger().info("DrySessionController closing");
    }

    @Override
    public String getHost() {
        return "dryhost";
    }

    @Override
    public int getPort() {
        return 22;
    }

    @Override
    public void put(InputStream source, String dest) {
    }
}
