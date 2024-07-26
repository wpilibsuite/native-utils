package edu.wpi.first.deployutils;

import com.jcraft.jsch.JSch;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import edu.wpi.first.deployutils.deploy.DeployPlugin;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public class DeployUtils implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        ETLoggerFactory.INSTANCE.addColorOutput(project);

        project.getPluginManager().apply(DeployPlugin.class);
    }

    private static JSch jsch;
    public static JSch getJsch() {
        if (jsch == null) jsch = new JSch();
        return jsch;
    }

    public static boolean isDryRun(Project project) {
        return project.hasProperty("deploy-dry");
    }

    public static boolean isSkipCache(Project project) {
        return project.hasProperty("deploy-dirty");
    }
}
