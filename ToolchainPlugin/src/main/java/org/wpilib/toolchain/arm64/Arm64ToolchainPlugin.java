package org.wpilib.toolchain.arm64;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.process.ExecOperations;

import org.wpilib.toolchain.NativePlatforms;
import org.wpilib.toolchain.ToolchainDescriptor;
import org.wpilib.toolchain.ToolchainExtension;
import org.wpilib.toolchain.configurable.CrossCompilerConfiguration;
import org.wpilib.toolchain.opensdk.OpenSdkToolchainBase;

public class Arm64ToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "arm64";
    public static final String baseToolchainName = "arm64-bookworm";

    private Arm64ToolchainExtension arm64Ext;
    private OpenSdkToolchainBase opensdk;
    private ExecOperations operations;

    @Inject
    public Arm64ToolchainPlugin(ExecOperations operations) {
        this.operations = operations;
    }

    @Override
    public void apply(Project project) {
        arm64Ext = project.getExtensions().create("arm64Toolchain", Arm64ToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        opensdk = new OpenSdkToolchainBase(baseToolchainName, arm64Ext, project, Arm64ToolchainExtension.INSTALL_SUBDIR,
                "bookworm", project.provider(() -> "aarch64-bookworm-linux-gnu"),
                toolchainExt.getToolchainGraphService(), operations);

        CrossCompilerConfiguration configuration = project.getObjects().newInstance(CrossCompilerConfiguration.class,
                NativePlatforms.linuxarm64);

        configuration.getArchitecture().set("arm64");
        configuration.getOperatingSystem().set("linux");
        configuration.getCompilerPrefix().set("");
        configuration.getOptional().convention(true);

        ToolchainDescriptor descriptor = new ToolchainDescriptor(
                project,
                toolchainName,
                toolchainName + "Gcc",
                configuration.getOptional());
        descriptor.getToolchainPlatform().set(NativePlatforms.linuxarm64);
        descriptor.getVersionLow().set(arm64Ext.getVersionLow());
        descriptor.getVersionHigh().set(arm64Ext.getVersionHigh());
        configuration.getToolchainDescriptor().set(descriptor);

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor descriptor) {
        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }

}
