package org.wpilib.toolchain;

import de.undercouch.gradle.tasks.download.DownloadAction;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.ArchiveOperations;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DeleteSpec;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class DefaultToolchainInstaller extends AbstractToolchainInstaller {

    private OperatingSystem os;
    private Provider<URL> sourceProvider;
    private Provider<File> installDirProvider;
    private String subdir;
    private final File gradleUserHomeDir;
    private final DownloadAction downloadAction;
    private final FileSystemOperations fileSystemOperations;
    private final ArchiveOperations archiveOperations;

    public static class ToolchainInstallerOptions {
        public OperatingSystem os;
        public Provider<URL> sourceProvider;
        public Provider<File> installDirProvider;
        public String subdir;
        public Project project;
    }

    @Inject
    public DefaultToolchainInstaller(ToolchainInstallerOptions options, FileSystemOperations fileSystemOperations, ArchiveOperations archiveOperations) {
        this.os = options.os;
        this.sourceProvider = options.sourceProvider;
        this.installDirProvider = options.installDirProvider;
        this.subdir = options.subdir;
        this.gradleUserHomeDir = options.project.getGradle().getGradleUserHomeDir();
        downloadAction = new DownloadAction(options.project);
        this.fileSystemOperations = fileSystemOperations;
        this.archiveOperations = archiveOperations;
    }

    @Override
    public void install() {
        URL source = sourceProvider.get();
        File cacheLoc = new File(gradleUserHomeDir, "cache");
        File dst = new File(cacheLoc, "download/" + source.getPath());
        dst.getParentFile().mkdirs();

        System.out.println("Downloading " + source.toString() + "... ");
        try {
            downloadAction.src(source);
            downloadAction.dest(dst);
            downloadAction.overwrite(false);
            downloadAction.retries(1);
            downloadAction.execute().get();
        } catch (IOException e) {
            throw new GradleException("Could not download toolchain", e);
        } catch (InterruptedException e) {
            throw new GradleException("Could not download toolchain, interrupted", e);
        } catch (ExecutionException e) {
            throw new GradleException("Could not download toolchain, failed", e);
        }

        if (downloadAction.isUpToDate()) {
            System.out.println("Already Downloaded!");
        }

        System.out.println("Extracting...");
        File extractDir = new File(cacheLoc, "extract/");
        if (extractDir.exists()) {
            fileSystemOperations.delete((DeleteSpec d) -> {
                d.delete(extractDir);
            });
        }
        extractDir.mkdirs();

        fileSystemOperations.copy((CopySpec c) -> {
            FileTree tree;
            if (dst.getName().endsWith(".tar.gz") || dst.getName().endsWith(".tgz")) {
                tree = archiveOperations.tarTree(archiveOperations.gzip(dst));
            } else {
                throw new GradleException("Don't know how to extract file type: " + dst.getName());
            }
            System.out.println(tree);
            c.from(tree);
            c.into(extractDir);
        });

        System.out.println("Copying...");
        File installDir = installDirProvider.get();
        if (installDir.exists()) {
            fileSystemOperations.delete((DeleteSpec d) -> {
                d.delete(installDir);
            });
        }
        installDir.mkdirs();

        fileSystemOperations.copy((CopySpec c) -> {
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
