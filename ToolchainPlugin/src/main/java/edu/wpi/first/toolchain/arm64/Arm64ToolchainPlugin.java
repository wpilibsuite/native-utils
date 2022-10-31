package edu.wpi.first.toolchain.arm64;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainRegistrar;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase;

public class Arm64ToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "arm64";
    public static final String baseToolchainName = "arm64-bullseye";

    private Arm64ToolchainExtension arm64Ext;
    private OpenSdkToolchainBase opensdk;

    @Override
    public void apply(Project project) {
        arm64Ext = project.getExtensions().create("arm64Toolchain", Arm64ToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        opensdk = new OpenSdkToolchainBase(baseToolchainName, arm64Ext, project, Arm64ToolchainExtension.INSTALL_SUBDIR,
                "bullseye", project.provider(() -> "aarch64-bullseye-linux-gnu"));

        CrossCompilerConfiguration configuration = project.getObjects().newInstance(CrossCompilerConfiguration.class, NativePlatforms.linuxarm64);

        configuration.getArchitecture().set("arm64");
        configuration.getOperatingSystem().set("linux");
        configuration.getCompilerPrefix().set("");
        configuration.getOptional().convention(true);

        ToolchainDescriptor<Arm64Gcc> descriptor = new ToolchainDescriptor<>(
                project,
                toolchainName,
                "arm64Gcc",
                new ToolchainRegistrar<Arm64Gcc>(Arm64Gcc.class, project),
                configuration.getOptional());
        descriptor.getToolchainPlatform().set(NativePlatforms.linuxarm64);
        descriptor.getVersionLow().set(arm64Ext.getVersionLow());
        descriptor.getVersionHigh().set(arm64Ext.getVersionHigh());
        configuration.getToolchainDescriptor().set(descriptor);

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor<Arm64Gcc> descriptor) {
        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }

}
