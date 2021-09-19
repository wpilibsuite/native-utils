package edu.wpi.first.toolchain.bionic;

import edu.wpi.first.toolchain.*;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.configurable.DefaultCrossCompilerConfiguration;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class BionicToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "bionic";

    private BionicToolchainExtension bionicExt;
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        bionicExt = project.getExtensions().create("bionicToolchain", BionicToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        Property<Boolean> optional = project.getObjects().property(Boolean.class);
        optional.set(true);

        ToolchainDescriptor<BionicGcc> descriptor = new ToolchainDescriptor<>(toolchainName, "bionicGcc", new ToolchainRegistrar<BionicGcc>(BionicGcc.class, project), optional);
        descriptor.setToolchainPlatforms(NativePlatforms.aarch64bionic);
        descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
            disc.configureVersions(bionicExt.versionLow, bionicExt.versionHigh);
        });

        CrossCompilerConfiguration configuration = new DefaultCrossCompilerConfiguration(NativePlatforms.aarch64bionic, descriptor, optional);
        configuration.setArchitecture("aarch64");
        configuration.setOperatingSystem("linux");
        configuration.setCompilerPrefix("");

        toolchainExt.getCrossCompilers().add(configuration);


        project.afterEvaluate((Project proj) -> {
            populateDescriptor(descriptor);
        });
    }

    public static File toolchainInstallLoc(String vers) {
        return new File(ToolchainPlugin.pluginHome(), vers);
    }

    public String composeTool(String toolName) {
        String bionicVersion = bionicExt.toolchainVersion.split("-")[0].toLowerCase();
        String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
        return "aarch64-" + bionicVersion + "-linux-gnu-" + toolName + exeSuffix;
    }

    public void populateDescriptor(ToolchainDescriptor<BionicGcc> descriptor) {
        String bionicVersion = bionicExt.toolchainVersion.split("-")[0].toLowerCase();
        File installLoc = toolchainInstallLoc(bionicVersion);

        descriptor.getDiscoverers().add(ToolchainDiscoverer.create("GradleUserDir", installLoc, this::composeTool, project));
        descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, this::composeTool));

        try {
            descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, installLoc, bionicVersion));
            descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, installLoc, bionicVersion));
            descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, installLoc, bionicVersion));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed Toolchain URL", e);
        }
    }

    private AbstractToolchainInstaller installerFor(OperatingSystem os, File installDir, String subdir) throws MalformedURLException {
        URL url = toolchainDownloadUrl(toolchainRemoteFile());
        return new DefaultToolchainInstaller(os, url, installDir, subdir);
    }

    private String toolchainRemoteFile() {
        String[] desiredVersion = bionicExt.toolchainVersion.split("-");

        String platformId;
        if (OperatingSystem.current().isWindows()) {
            platformId = "Windows" + (NativePlatforms.desktopPlatformArch() == "x86-64" ? "64" : "32");
        } else if (OperatingSystem.current().isMacOsX()) {
            platformId = "Mac";
        } else {
            platformId = "Linux";
        }
        String ext = OperatingSystem.current().isWindows() ? "zip" : "tar.gz";
        return desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "." + ext;
    }

    private URL toolchainDownloadUrl(String file) throws MalformedURLException {
        return new URL("https://github.com/wpilibsuite/aarch64-bionic-toolchain/releases/download/" + bionicExt.toolchainTag + "/" + file);
    }

}
