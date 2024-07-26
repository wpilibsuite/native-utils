package edu.wpi.first.deployutils.deploy;

import java.util.Objects;

public class CommandDeployResult {
    private final String command;

    public final String getCommand() {
        return command;
    }

    private final String result;

    public final String getResult() {
        return result;
    }

    private final int exitCode;

    public final int getExitCode() {
        return exitCode;
    }

    public CommandDeployResult(String command, String result, int exitCode) {
        this.command = command;
        this.result = result;
        this.exitCode = exitCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, result, exitCode);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CommandDeployResult) {
            CommandDeployResult cdr = (CommandDeployResult)other;
            return Objects.equals(command, cdr.command) &&
                   Objects.equals(result, cdr.result) &&
                   exitCode == cdr.exitCode;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CommandDeployResult(" + getCommand() + ", " + getResult() + ", " + getExitCode() + ")";
    }

}
