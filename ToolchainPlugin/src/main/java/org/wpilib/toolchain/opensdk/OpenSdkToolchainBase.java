package org.wpilib.toolchain.opensdk;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.ExecOperations;

import org.wpilib.toolchain.AbstractToolchainInstaller;
import org.wpilib.toolchain.DefaultToolchainInstaller;
import org.wpilib.toolchain.DefaultToolchainInstaller.ToolchainInstallerOptions;
import org.wpilib.toolchain.NativePlatforms;
import org.wpilib.toolchain.ToolchainDescriptor;
import org.wpilib.toolchain.ToolchainDiscoverer;
import org.wpilib.toolchain.ToolchainGraphBuildService;
import org.wpilib.toolchain.ToolchainPlugin;

public class OpenSdkToolchainBase {
    private final String baseToolchainName;
    private final OpenSdkToolchainExtension tcExt;
    private final Project project;
    private final String installSubdir;
    private final String archiveSubDir;
    private final Provider<String> toolchainPrefix;
    private final ToolchainGraphBuildService rootExtension;
    private final ExecOperations operations;
    private final ObjectFactory objectFactory;

    public static class ToolchainBaseOptions {
        public String baseToolchainName;
        public OpenSdkToolchainExtension tcExt;
        public Project project;
        public String installSubdir;
        public String archiveSubDir;
        public Provider<String> toolchainPrefix;
        public ToolchainGraphBuildService rootExtension;
    }

    @Inject
    public OpenSdkToolchainBase(ToolchainBaseOptions options, ExecOperations operations, ObjectFactory objectFactory) {
        this.baseToolchainName = options.baseToolchainName;
        this.tcExt = options.tcExt;
        this.project = options.project;
        this.installSubdir = options.installSubdir;
        this.archiveSubDir = options.archiveSubDir;
        this.toolchainPrefix = options.toolchainPrefix;
        this.rootExtension = options.rootExtension;
        this.operations = operations;
        this.objectFactory = objectFactory;
    }

    private String toolchainRemoteFile() {
        String[] desiredVersion = tcExt.getToolchainVersion().get().split("-");

        String platformId;
        if (OperatingSystem.current().isWindows()) {
            platformId = "x86_64-w64-mingw32";
        } else if (OperatingSystem.current().isMacOsX()) {
            platformId = (NativePlatforms.desktopPlatformArch(operations) == NativePlatforms.x64arch ? "x86_64" : "arm64")
                    + "-apple-darwin";
        } else {
            String desktopPlatformArch = NativePlatforms.desktopPlatformArch(operations);
            if (desktopPlatformArch.equals(NativePlatforms.arm64arch)) {
                platformId = "aarch64-trixie-linux-gnu";
            } else {
                platformId = "x86_64-linux-gnu";
            }

        }
        String ext = "tgz";
        return baseToolchainName + "-" + desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "."
                + ext;
    }

    public URL toolchainDownloadUrl() {
        String file = toolchainRemoteFile();
        try {
            return URI.create("https://github.com/wpilibsuite/opensdk/releases/download/"
                    + tcExt.getToolchainTag().get() + "/" + file).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String composeTool(String toolName) {
        String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
        return toolchainPrefix.get() + "-" + toolName + exeSuffix;
    }

    public File toolchainInstallLoc(String year, String installSubdir) {
        return new File(ToolchainPlugin.pluginHome(project), "first/" + year + "/" + installSubdir);
    }

    public AbstractToolchainInstaller installerFor(OperatingSystem os, Provider<File> installDir, String subdir)
            throws MalformedURLException {
                ToolchainInstallerOptions options = new ToolchainInstallerOptions();
                options.os = os;
                options.sourceProvider = project.provider(this::toolchainDownloadUrl);
                options.installDirProvider = installDir;
                options.subdir = subdir;
                options.project = project;
                return objectFactory.newInstance(DefaultToolchainInstaller.class, options);
    }

    public void populatePathAndDownloadDescriptors(ToolchainDescriptor descriptor) {
        Provider<File> fp = project.provider(() -> {
            String year = tcExt.getToolchainVersion().get().split("-")[0].toLowerCase();
            File installLoc = toolchainInstallLoc(year, installSubdir);
            return installLoc;
        });

        // Discoverer order matters. They will be searched from top to bottom.
        descriptor.getDiscoverers()
                .add(ToolchainDiscoverer.createProperty("GradleUserDir", descriptor, fp, this::composeTool, project));
        descriptor.getDiscoverers()
                .add(ToolchainDiscoverer.forSystemPath(project, rootExtension, descriptor, this::composeTool, operations));

        try {
            descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, fp, archiveSubDir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, fp, archiveSubDir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, fp, archiveSubDir));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed Toolchain URL", e);
        }
    }
}
