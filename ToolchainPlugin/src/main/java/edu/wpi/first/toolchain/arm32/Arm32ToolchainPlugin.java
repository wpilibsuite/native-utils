package edu.wpi.first.toolchain.arm32;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.process.ExecOperations;

import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase;

public class Arm32ToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "arm32";
    public static final String baseToolchainName = "armhf-raspi-bullseye";

    private Arm32ToolchainExtension arm32Ext;
    private OpenSdkToolchainBase opensdk;

    private ExecOperations operations;

    @Inject
    public Arm32ToolchainPlugin(ExecOperations operations) {
        this.operations = operations;
    }

    @Override
    public void apply(Project project) {

        arm32Ext = project.getExtensions().create("arm32Toolchain", Arm32ToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        opensdk = new OpenSdkToolchainBase(baseToolchainName, arm32Ext, project, Arm32ToolchainExtension.INSTALL_SUBDIR,
                "raspi-bullseye", project.provider(() -> "armv6-bullseye-linux-gnueabihf"),
                toolchainExt.getToolchainGraphService(), operations);

        CrossCompilerConfiguration configuration = project.getObjects().newInstance(CrossCompilerConfiguration.class,
                NativePlatforms.linuxarm32);

        configuration.getArchitecture().set("arm");
        configuration.getOperatingSystem().set("linux");
        configuration.getCompilerPrefix().set("");
        configuration.getOptional().convention(true);

        ToolchainDescriptor descriptor = new ToolchainDescriptor(
                project,
                toolchainName,
                toolchainName + "Gcc",
                configuration.getOptional());
        descriptor.getToolchainPlatform().set(NativePlatforms.linuxarm32);
        descriptor.getVersionLow().set(arm32Ext.getVersionLow());
        descriptor.getVersionHigh().set(arm32Ext.getVersionHigh());
        configuration.getToolchainDescriptor().set(descriptor);

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor descriptor) {
        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }

}
