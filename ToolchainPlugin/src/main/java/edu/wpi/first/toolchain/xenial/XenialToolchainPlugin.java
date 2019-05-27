package edu.wpi.first.toolchain.xenial;

import edu.wpi.first.toolchain.*;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.configurable.DefaultCrossCompilerConfiguration;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class XenialToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "xenial";

    private XenialToolchainExtension xenialExt;
    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;

        xenialExt = project.getExtensions().create("xenialToolchain", XenialToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        ToolchainDescriptor<XenialGcc> descriptor = new ToolchainDescriptor<>(toolchainName, "xenialGcc", new ToolchainRegistrar<XenialGcc>(XenialGcc.class, project), xenialExt.IsOptional());
        descriptor.setToolchainPlatforms(NativePlatforms.xenial);
        descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
            disc.configureVersions(xenialExt.versionLow, xenialExt.versionHigh);
        });

        CrossCompilerConfiguration configuration = new DefaultCrossCompilerConfiguration(NativePlatforms.xenial, xenialExt.IsOptional(), descriptor);
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
        String xenialVersion = xenialExt.toolchainVersion.split("-")[0].toLowerCase();
        String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
        return "aarch64-" + xenialVersion + "-linux-gnu-" + toolName + exeSuffix;
    }

    public void populateDescriptor(ToolchainDescriptor<XenialGcc> descriptor) {
        String xenialVersion = xenialExt.toolchainVersion.split("-")[0].toLowerCase();
        File installLoc = toolchainInstallLoc(xenialVersion);

        descriptor.getDiscoverers().add(ToolchainDiscoverer.create("GradleUserDir", installLoc, this::composeTool, project));
        descriptor.getDiscoverers().addAll(ToolchainDiscoverer.forSystemPath(project, this::composeTool));

        try {
            descriptor.getInstallers().add(installerFor(OperatingSystem.LINUX, installLoc, xenialVersion));
            descriptor.getInstallers().add(installerFor(OperatingSystem.WINDOWS, installLoc, xenialVersion));
            descriptor.getInstallers().add(installerFor(OperatingSystem.MAC_OS, installLoc, xenialVersion));
        } catch (MalformedURLException e) {
            throw new GradleException("Malformed Toolchain URL", e);
        }
    }

    private AbstractToolchainInstaller installerFor(OperatingSystem os, File installDir, String subdir) throws MalformedURLException {
        URL url = toolchainDownloadUrl(toolchainRemoteFile());
        return new DefaultToolchainInstaller(os, url, installDir, subdir);
    }

    private String toolchainRemoteFile() {
        String[] desiredVersion = xenialExt.toolchainVersion.split("-");

        String platformId = OperatingSystem.current().isWindows() ? "Windows" : OperatingSystem.current().isMacOsX() ? "Mac" : "Linux";
        String ext = OperatingSystem.current().isWindows() ? "zip" : "tar.gz";
        return desiredVersion[0] + "-" + platformId + "-Toolchain-" + desiredVersion[1] + "." + ext;
    }

    private URL toolchainDownloadUrl(String file) throws MalformedURLException {
        return new URL("https://github.com/wpilibsuite/aarch64-xenial-toolchain/releases/download/" + xenialExt.toolchainTag + "/" + file);
    }

}
