package edu.wpi.first.toolchain;

import de.undercouch.gradle.tasks.download.DownloadAction;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DefaultToolchainInstaller extends AbstractToolchainInstaller {

    private OperatingSystem os;
    private Provider<URL> sourceProvider;
    private Provider<File> installDirProvider;
    private String subdir;

    public DefaultToolchainInstaller(OperatingSystem os, Provider<URL> source, Provider<File> installDir, String subdir) {
        this.os = os;
        this.sourceProvider = source;
        this.installDirProvider = installDir;
        this.subdir = subdir;
    }

    @Override
    public void install(Project project) {
        URL source = sourceProvider.get();
        File cacheLoc = new File(ToolchainPlugin.gradleHome(), "cache");
        File dst = new File(cacheLoc, "download/" + source.getPath());
        dst.getParentFile().mkdirs();

        System.out.println("Downloading " + source.toString() + "... ");
        DownloadAction action = new DownloadAction(project);
        try {
            action.src(source);
            action.dest(dst);
            action.overwrite(false);
            action.execute();
        } catch (IOException e) {
            throw new GradleException("Could not download toolchain", e);
        }

        if (action.isUpToDate()) {
            System.out.println("Already Downloaded!");
        }

        System.out.println("Extracting...");
        File extractDir = new File(cacheLoc, "extract/");
        if (extractDir.exists())
            project.delete(extractDir.getAbsolutePath());
        extractDir.mkdirs();

        project.copy((CopySpec c) -> {
            FileTree tree;
            if (dst.getName().endsWith(".tar.gz") || dst.getName().endsWith(".tgz"))
                tree = project.tarTree(project.getResources().gzip(dst));
            else if (dst.getName().endsWith(".zip"))
                tree = project.zipTree(dst);
            else
                throw new GradleException("Don't know how to extract file type: " + dst.getName());

            System.out.println(tree);
            c.from(tree);
            c.into(extractDir);
        });

        System.out.println("Copying...");
        File installDir = installDirProvider.get();
        if (installDir.exists())
            project.delete(installDir.getAbsolutePath());
        installDir.mkdirs();

        project.copy((CopySpec c) -> {
            c.from(new File(extractDir, subdir));
            c.into(installDir);
        });

        System.out.println("Done! Installed to: " + installDir.getAbsolutePath());
    }

    @Override
    public boolean targets(OperatingSystem os) {
        return os.getName().equals(this.os.getName());
    }

    @Override
    public File sysrootLocation() {
        return installDirProvider.get();
    }
}
