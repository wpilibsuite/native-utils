package edu.wpi.first.toolchain.roborio;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptor;
import edu.wpi.first.toolchain.ToolchainDiscoverer;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.ToolchainRegistrar;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.opensdk.OpenSdkToolchainBase;

public class RoboRioToolchainPlugin implements Plugin<Project> {

    public static final String toolchainName = "roboRio";
    public static final String baseToolchainName = "cortexa9_vfpv3-roborio-academic";

    private RoboRioToolchainExtension roborioExt;
    private Project project;
    private OpenSdkToolchainBase opensdk;

    @Override
    public void apply(Project project) {
        this.project = project;

        roborioExt = project.getExtensions().create("frcToolchain", RoboRioToolchainExtension.class);

        ToolchainExtension toolchainExt = project.getExtensions().getByType(ToolchainExtension.class);

        Provider<String> prefixProvider = project.provider(() -> {
            String year = roborioExt.getToolchainVersion().get().split("-")[0].toLowerCase();
            String prefix = "arm-frc" + year + "-linux-gnueabi";
            return prefix;
        });

        opensdk = new OpenSdkToolchainBase(baseToolchainName, roborioExt, project,
                RoboRioToolchainExtension.INSTALL_SUBDIR, "roborio-academic", prefixProvider);

        CrossCompilerConfiguration configuration = project.getObjects().newInstance(CrossCompilerConfiguration.class, NativePlatforms.roborio);

        configuration.getArchitecture().set("arm");
        configuration.getOperatingSystem().set("linux");
        configuration.getCompilerPrefix().set("");
        configuration.getOptional().convention(true);

        ToolchainDescriptor<RoboRioGcc> descriptor = new ToolchainDescriptor<>(
                project,
                toolchainName,
                "roborioGcc",
                new ToolchainRegistrar<RoboRioGcc>(RoboRioGcc.class, project),
                configuration.getOptional());
        descriptor.getToolchainPlatform().set(NativePlatforms.roborio);
        descriptor.getVersionLow().set(roborioExt.getVersionLow());
        descriptor.getVersionHigh().set(roborioExt.getVersionHigh());
        configuration.getToolchainDescriptor().set(descriptor);

        toolchainExt.getCrossCompilers().add(configuration);

        populateDescriptor(descriptor);
    }

    public void populateDescriptor(ToolchainDescriptor<RoboRioGcc> descriptor) {
        Provider<File> fp = project.provider(() -> {
            String year = roborioExt.getToolchainVersion().get().split("-")[0].toLowerCase();
            File frcHomeLoc = new File(new FrcHome(year).get(), "roborio");
            return frcHomeLoc;
        });

        // Add FRC Home first, as we want it searched first
        descriptor.getDiscoverers().add(ToolchainDiscoverer.createProperty("FRCHome", descriptor, fp, opensdk::composeTool, project));

        opensdk.populatePathAndDownloadDescriptors(descriptor);
    }
}
