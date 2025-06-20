package edu.wpi.first.toolchain.systemcore;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.process.ExecOperations;

import edu.wpi.first.toolchain.FrcHome;
import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase;

public class SystemCoreToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "systemCore";
    public static final String baseToolchainName = "arm64-bookworm";

    private SystemCoreToolchainExtension systemcoreExt;
    private Project project;
    private OpenSdkToolchainBase opensdk;
    private ExecOperations operations;

    @Inject
    public SystemCoreToolchainPlugin(ExecOperations operations) {
        this.operations = operations;
    }

    @Override
    public void apply(Project project) {
        this.project = project;

        systemcoreExt = project.getExtensions().create("systemcoreToolchain", SystemCoreToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        opensdk = new OpenSdkToolchainBase(baseToolchainName, systemcoreExt, project,
                SystemCoreToolchainExtension.INSTALL_SUBDIR, "bookworm", project.provider(() -> "aarch64-bookworm-linux-gnu"), toolchainExt.getToolchainGraphService(), operations);

        CrossCompilerConfiguration configuration = project.getObjects().newInstance(CrossCompilerConfiguration.class, NativePlatforms.systemcore);

        configuration.getArchitecture().set("arm64");
        configuration.getOperatingSystem().set("linux");
        configuration.getCompilerPrefix().set("");
        configuration.getOptional().convention(true);

        ToolchainDescriptor descriptor = new ToolchainDescriptor(
                project,
                toolchainName,
                toolchainName + "Gcc",
                configuration.getOptional());
        descriptor.getToolchainPlatform().set(NativePlatforms.systemcore);
        descriptor.getVersionLow().set(systemcoreExt.getVersionLow());
        descriptor.getVersionHigh().set(systemcoreExt.getVersionHigh());
        configuration.getToolchainDescriptor().set(descriptor);

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor descriptor) {
        Provider<File> fp = project.provider(() -> {
            String year = "2027_alpha1";
            File frcHomeLoc = new File(new FrcHome(year).get(), "systemcore");
            return frcHomeLoc;
        });

        // Add FRC Home first, as we want it searched first
        descriptor.getDiscoverers().add(ToolchainDiscoverer.createProperty("FRCHome", descriptor, fp, opensdk::composeTool, project));

        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }
}
