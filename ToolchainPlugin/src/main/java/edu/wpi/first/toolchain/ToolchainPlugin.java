package edu.wpi.first.toolchain;

//import edu.wpi.first.deployutils.toolchains.ToolchainsPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.provider.Provider;
import org.gradle.internal.logging.text.TreeFormatter;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ToolchainPlugin implements Plugin<Project> {
    private ToolchainExtension ext;

    @Override
    public void apply(Project project) {
        Provider<ToolchainGraphBuildService> serviceProvider = project.getGradle().getSharedServices().registerIfAbsent("toolchainGraph", ToolchainGraphBuildService.class, spec -> {
        });
        serviceProvider.get().configure(project.getGradle());

        ext = project.getExtensions().create("toolchainsPlugin", ToolchainExtension.class, project, serviceProvider.get());

        project.getTasks().register("explainToolchains", (Task t) -> {
            t.setGroup("Toolchains");
            t.setDescription("Explain Toolchains Plugin extension");
            t.usesService(serviceProvider);

            t.doLast((task) -> {
                TreeFormatter formatter = new TreeFormatter();
                ext.explain(formatter);
                System.out.println(formatter.toString());
            });
        });

        ext.getToolchainDescriptors().all((ToolchainDescriptorBase desc) -> {
            if (serviceProvider.get().registerInstallTask(desc.getInstallTaskName())) {
                project.getTasks().register(desc.getInstallTaskName(), InstallToolchainTask.class, (InstallToolchainTask t) -> {
                    t.setGroup("Toolchains");
                    t.setDescription("Install Toolchain for " + desc.getName() + " if installers are available.");
                    t.setDescriptor(desc);
                    t.usesService(serviceProvider);
                });
            }
        });

        project.getGradle().getTaskGraph().whenReady((TaskExecutionGraph graph) -> {
            // Sort into buckets based on the descriptor, then cancel all but the first entry in each
            // of those sublists, ensuring only one instance of each install task may run.
            List<InstallToolchainTask> installTasks = graph.getAllTasks().stream()
                    .filter(t -> t instanceof InstallToolchainTask)
                    .map(t -> (InstallToolchainTask) t)
                    .collect(Collectors.toList());

            installTasks.stream()
                    .collect(Collectors.groupingBy(InstallToolchainTask::getDescriptorName))
                    .values()
                    .forEach((list) -> {
                        list.stream().skip(1).forEach(t -> t.setEnabled(false));
                    });

            // Cancel all non-install tasks.
            if (installTasks.size() > 0)
                graph.getAllTasks().stream()
                    .filter(t -> !(t instanceof InstallToolchainTask))
                    .forEach(t -> {
                        System.out.println("Cancelling: " + t.getName());
                        t.setEnabled(false);
                    });
        });

        project.getPluginManager().apply(ToolchainRules.class);
        //project.getPluginManager().apply(ToolchainsPlugin.class);
    }

    public static File gradleHome() {
        return new File(System.getProperty("user.home"), ".gradle");
    }

    public static File pluginHome() {
        return new File(gradleHome(), "toolchains");
    }

}
