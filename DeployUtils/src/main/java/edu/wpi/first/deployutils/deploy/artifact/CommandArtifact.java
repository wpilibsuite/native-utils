package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import edu.wpi.first.deployutils.deploy.CommandDeployResult;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class CommandArtifact extends AbstractArtifact {

    private String command = null;
    private CommandDeployResult result = null;

    @Inject
    public CommandArtifact(String name, RemoteTarget target) {
        super(name, target);
    }

    @Override
    public void deploy(DeployContext context) {
        this.result = context.execute(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandDeployResult getResult() {
        return result;
    }

}
