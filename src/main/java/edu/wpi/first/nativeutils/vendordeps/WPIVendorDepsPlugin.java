package edu.wpi.first.nativeutils.vendordeps;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WPIVendorDepsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("wpiVendorDeps", WPIVendorDepsExtension.class, project);


        project.getTasks().register("vendordep", VendorDepTask.class, task -> {
            task.setGroup("NativeUtils");
            task.setDescription("Install vendordep JSON file from URL or local wpilib folder");
        });
    }
}
