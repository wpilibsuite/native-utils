package edu.wpi.first.toolchain.arm32;

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

public class Arm32ToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "arm32";
    public static final String baseToolchainName = "armhf-raspi-bullseye";

    private Arm32ToolchainExtension arm32Ext;
    private OpenSdkToolchainBase opensdk;

    @Override
    public void apply(Project project) {

        arm32Ext = project.getExtensions().create("arm32Toolchain", Arm32ToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        opensdk = new OpenSdkToolchainBase(baseToolchainName, arm32Ext, project, Arm32ToolchainExtension.INSTALL_SUBDIR,
                "raspi-bullseye", "armv6-bullseye-linux-gnueabihf");

        Property<Boolean> optional = project.getObjects().property(Boolean.class);
        optional.set(true);

        ToolchainDescriptor<Arm32Gcc> descriptor = new ToolchainDescriptor<>(
                project,
                toolchainName,
                "arm32Gcc",
                new ToolchainRegistrar<Arm32Gcc>(Arm32Gcc.class, project),
                optional);
        descriptor.setToolchainPlatforms(NativePlatforms.linuxarm32);
        descriptor.getDiscoverers().all((ToolchainDiscoverer disc) -> {
            disc.configureVersions(arm32Ext.versionLow, arm32Ext.versionHigh);
        });

        CrossCompilerConfiguration configuration = new DefaultCrossCompilerConfiguration(NativePlatforms.linuxarm32,
                descriptor, optional);
        configuration.setArchitecture("arm");
        configuration.setOperatingSystem("linux");
        configuration.setCompilerPrefix("");

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor<Arm32Gcc> descriptor) {
        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }

}
