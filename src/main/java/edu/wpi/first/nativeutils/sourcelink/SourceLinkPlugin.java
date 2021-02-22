package edu.wpi.first.nativeutils.sourcelink;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.internal.os.OperatingSystem;

public class SourceLinkPlugin implements Plugin<Project> {
    public static final String SOURCE_LINK_ROOT_TASK_NAME = "generateSourceLinkFile";

    private File getGitDir(File currentDir) {
        if (new File(currentDir, ".git").exists()) {
            return currentDir;
        }

        File parentFile = currentDir.getParentFile();

        if (parentFile == null) {
            return null;
        }

        return getGitDir(parentFile);
    }

    @Override
    public void apply(Project project) {
        if (!OperatingSystem.current().isWindows()) {
            return;
        }

        try {
            project.getRootProject().getTasks().named(SOURCE_LINK_ROOT_TASK_NAME, SourceLinkGenerationTask.class);
            project.getPluginManager().apply(SourceLinkRules.class);
        } catch (UnknownTaskException notfound) {
            File gitDir = getGitDir(project.getRootProject().getRootDir());
            if (gitDir == null) {
                System.out.println("No .git directory was found in" + project.getRootProject().getRootDir().toString()
                        + "or any parent directories of that directory.");
                System.out.println("SourceLink generation skipped");
            } else {
                project.getRootProject().getTasks().register(SOURCE_LINK_ROOT_TASK_NAME, SourceLinkGenerationTask.class,
                        gitDir);

                project.getPluginManager().apply(SourceLinkRules.class);
            }
        }
    }
}
