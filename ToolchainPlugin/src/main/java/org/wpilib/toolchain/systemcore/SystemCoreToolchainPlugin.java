package org.wpilib.toolchain.systemcore;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.process.ExecOperations;

import org.wpilib.toolchain.WPILibHome;
import org.wpilib.toolchain.NativePlatforms;
import org.wpilib.toolchain.ToolchainDescriptor;
import org.wpilib.toolchain.ToolchainDiscoverer;
import org.wpilib.toolchain.ToolchainExtension;
import org.wpilib.toolchain.configurable.CrossCompilerConfiguration;
import org.wpilib.toolchain.opensdk.OpenSdkToolchainBase;
import org.wpilib.toolchain.opensdk.OpenSdkToolchainBase.ToolchainBaseOptions;

public class SystemCoreToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "systemCore";
    public static final String baseToolchainName = "arm64-systemcore";

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

        ToolchainBaseOptions options = new ToolchainBaseOptions();
        options.baseToolchainName = baseToolchainName;
        options.tcExt = systemcoreExt;
        options.project = project;
        options.installSubdir = SystemCoreToolchainExtension.INSTALL_SUBDIR;
        options.toolchainPrefix = project.provider(() -> "aarch64-systemcore2027-linux-gnu");
        options.rootExtension = toolchainExt.getToolchainGraphService();

        opensdk = project.getObjects().newInstance(OpenSdkToolchainBase.class, options);

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
            String year = "2027_alpha7";
            File wpilibHomeLoc = new File(new WPILibHome(year).get(), "systemcore");
            return wpilibHomeLoc;
        });

        // Add WPILib Home first, as we want it searched first
        descriptor.getDiscoverers().add(ToolchainDiscoverer.createProperty("WPILibHome", descriptor, fp, opensdk::composeTool, project));

        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }
}
