package edu.wpi.first.toolchain.roborio;

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

public class RoboRioToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "roboRio";

    private RoboRioToolchainExtension roborioExt;
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        roborioExt = project.getExtensions().create("frcToolchain", RoboRioToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        Property<Boolean> optional = project.getObjects().property(Boolean.class);
        optional.set(true);

        ToolchainDescriptor<RoboRioGcc> descriptor = new ToolchainDescriptor<>(
            project,
            toolchainName,
            "roborioGcc",
            new ToolchainRegistrar<RoboRioGcc>(RoboRioGcc.class, project),
            optional);
        descriptor.setToolchainPlatforms(NativePlatforms.roborio);
        descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
            disc.configureVersions(roborioExt.versionLow, roborioExt.versionHigh);
        });

        CrossCompilerConfiguration configuration = new DefaultCrossCompilerConfiguration(NativePlatforms.roborio, descriptor, optional);
        configuration.setArchitecture("arm");
        configuration.setOperatingSystem("linux");
        configuration.setCompilerPrefix("");

        toolchainExt.getCrossCompilers().add(configuration);

        project.afterEvaluate((Project proj) -> {
            populateDescriptor(descriptor);
        });
    }

    public static File toolchainInstallLoc(String year) {
        return new File(ToolchainPlugin.pluginHome(), "frc/" + year + "/roborio-academic");
    }

    public String composeTool(String toolName) {
        String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
        return "arm-frc" + roborioExt.year + "-linux-gnueabi-" + toolName + exeSuffix;
    }


    public void populateDescriptor(ToolchainDescriptor<RoboRioGcc> descriptor) {
        File frcHomeLoc = new File(new FrcHome(roborioExt.year).get(), "roborio-academic");
        File installLoc = toolchainInstallLoc(roborioExt.year);

        descriptor.getDiscoverers().add(ToolchainDiscoverer.create("FRCHome", frcHomeLoc, this::composeTool, project));
        descriptor.getDiscoverers().add(ToolchainDiscoverer.create("GradleUserDir", installLoc, this::composeTool, project));
        descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, this::composeTool));

        String installerSubdir = "roborio-academic";
        try {
            descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, installLoc, installerSubdir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, installLoc, installerSubdir));
            descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, installLoc, installerSubdir));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed Toolchain URL", e);
        }
    }

    private AbstractToolchainInstaller installerFor(OperatingSystem os, File installDir, String subdir) throws MalformedURLException {
        return new DefaultToolchainInstaller(os, this::toolchainDownloadUrl, installDir, subdir);
    }

    private final String baseToolchainName = "cortexa9_vfpv3-roborio-academic-";

    private String toolchainRemoteFile() {
        String[] desiredVersion = roborioExt.toolchainVersion.split("-");

        String platformId;
        if (OperatingSystem.current().isWindows()) {
            platformId = "x86_64-w64-mingw32";
        } else if (OperatingSystem.current().isMacOsX()) {
            platformId = (NativePlatforms.desktopPlatformArch(project) == "x86-64" ? "x86_64" : "arm64") + "-apple-darwin";
        } else {
            platformId = "x86_64-linux-gnu";
        }
        String ext = OperatingSystem.current().isWindows() ? "zip" : "tgz";
        return baseToolchainName + desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "." + ext;
    }

    private URL toolchainDownloadUrl() {
        String file = toolchainRemoteFile();
        try {
            return new URL("https://github.com/wpilibsuite/opensdk/releases/download/" + roborioExt.toolchainTag + "/" + file);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
