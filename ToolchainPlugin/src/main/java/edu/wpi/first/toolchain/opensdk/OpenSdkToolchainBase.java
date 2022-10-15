package edu.wpi.first.toolchain.opensdk;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;

import edu.wpi.first.toolchain.AbstractToolchainInstaller;
import edu.wpi.first.toolchain.DefaultToolchainInstaller;
import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainPlugin;

public class OpenSdkToolchainBase {
    private final String baseToolchainName;
    private final OpenSdkToolchainExtension tcExt;
    private final Project project;
    private final String installSubdir;
    private final String archiveSubDir;
    private final Provider<String> toolchainPrefix;

    public OpenSdkToolchainBase(String baseToolchainName, OpenSdkToolchainExtension tcExt, Project project,
            String installSubdir, String archiveSubdir, Provider<String> toolchainPrefix) {
        this.baseToolchainName = baseToolchainName;
        this.tcExt = tcExt;
        this.project = project;
        this.installSubdir = installSubdir;
        this.archiveSubDir = archiveSubdir;
        this.toolchainPrefix = toolchainPrefix;
    }

    private String toolchainRemoteFile() {
        String[] desiredVersion = tcExt.getToolchainVersion().get().split("-");

        String platformId;
        if (OperatingSystem.current().isWindows()) {
            platformId = "x86_64-w64-mingw32";
        } else if (OperatingSystem.current().isMacOsX()) {
            platformId = (NativePlatforms.desktopPlatformArch(project) == "x86-64" ? "x86_64" : "arm64")
                    + "-apple-darwin";
        } else {
            platformId = "x86_64-linux-gnu";
        }
        String ext = OperatingSystem.current().isWindows() ? "zip" : "tgz";
        return baseToolchainName + "-" + desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "."
                + ext;
    }

    public URL toolchainDownloadUrl() {
        String file = toolchainRemoteFile();
        try {
            return new URL("https://github.com/wpilibsuite/opensdk/releases/download/" + tcExt.getToolchainTag().get()
                    + "/" + file);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String composeTool(String toolName) {
        String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
        return toolchainPrefix.get() + "-" + toolName + exeSuffix;
    }

    public File toolchainInstallLoc(String year) {
        return new File(ToolchainPlugin.pluginHome(), "frc/" + year + "/" + installSubdir);
    }

    public AbstractToolchainInstaller installerFor(OperatingSystem os, Provider<File> installDir, String subdir)
            throws MalformedURLException {
        return new DefaultToolchainInstaller(os, project.provider(this::toolchainDownloadUrl), installDir, subdir);
    }

    public void populatePathAndDownloadDescriptors(ToolchainDescriptor<?> descriptor) {
        Provider<File> fp = project.provider(() -> {
            String year = tcExt.getToolchainVersion().get().split("-")[0].toLowerCase();
            File installLoc = toolchainInstallLoc(year);
            return installLoc;
        });

        descriptor.getDiscoverers().add(ToolchainDiscoverer.create("GradleUserDir", fp, this::composeTool, project));
        descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, this::composeTool));

        try {
            descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, fp, archiveSubDir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, fp, archiveSubDir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, fp, archiveSubDir));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed Toolchain URL", e);
        }
    }
}
