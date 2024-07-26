package edu.wpi.first.deployutils.deploy;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;

public class DeployPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);

        project.getExtensions().create("deploy", DeployExtension.class, project);
    }
}
