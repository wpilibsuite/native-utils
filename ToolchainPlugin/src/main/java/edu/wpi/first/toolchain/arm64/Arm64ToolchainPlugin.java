package edu.wpi.first.toolchain.arm64;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainRegistrar;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.configurable.DefaultCrossCompilerConfiguration;
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
                "bullseye", "aarch64-bullseye-linux-gnu");

        Property<Boolean> optional = project.getObjects().property(Boolean.class);
        optional.set(true);

        ToolchainDescriptor<Arm64Gcc> descriptor = new ToolchainDescriptor<>(
                project,
                toolchainName,
                "arm64Gcc",
                new ToolchainRegistrar<Arm64Gcc>(Arm64Gcc.class, project),
                optional);
        descriptor.setToolchainPlatforms(NativePlatforms.linuxarm64);
        descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
            disc.configureVersions(arm64Ext.versionLow, arm64Ext.versionHigh);
        });

        CrossCompilerConfiguration configuration = new DefaultCrossCompilerConfiguration(NativePlatforms.linuxarm64,
                descriptor, optional);
        configuration.setArchitecture("arm64");
        configuration.setOperatingSystem("linux");
        configuration.setCompilerPrefix("");

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor<Arm64Gcc> descriptor) {
        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }

}
