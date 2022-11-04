package edu.wpi.first.toolchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.internal.logging.text.DiagnosticsVisitor;
import org.gradle.internal.os.OperatingSystem;

import edu.wpi.first.toolchain.arm32.Arm32ToolchainPlugin;
import edu.wpi.first.toolchain.arm64.Arm64ToolchainPlugin;
import edu.wpi.first.toolchain.configurable.ConfigurableGcc;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.roborio.RoboRioToolchainPlugin;

public class ToolchainExtension {
    private final NamedDomainObjectContainer<CrossCompilerConfiguration> crossCompilers;
    private final NamedDomainObjectContainer<ToolchainDescriptorBase> toolchainDescriptors;
    private final Map<String, List<String>> stripExcludeMap = new HashMap<>();

    private Project project;

    public boolean registerPlatforms = true;
    public boolean registerReleaseBuildType = true;
    public boolean registerDebugBuildType = true;
    private final ToolchainRootExtension rootExtension;

    public ToolchainRootExtension getRootExtension() {
        return rootExtension;
    }

    public ToolchainExtension(Project project, ToolchainRootExtension rootExtension) {
        this.project = project;
        this.rootExtension = rootExtension;

        crossCompilers = project.container(CrossCompilerConfiguration.class, name -> {
            return project.getObjects().newInstance(CrossCompilerConfiguration.class, name);
        });

        toolchainDescriptors = project.container(ToolchainDescriptorBase.class);

        crossCompilers.all(config -> {
            if (!config.getToolchainDescriptor().isPresent()) {
                config.getOptional().convention(true);
                ToolchainDescriptor<ConfigurableGcc> descriptor = new ToolchainDescriptor<>(
                        project,
                        config.getName(),
                        config.getName() + "ConfiguredGcc",
                        new ToolchainRegistrar<ConfigurableGcc>(ConfigurableGcc.class, project),
                        config.getOptional());

                descriptor.getVersionLow().convention("0.0");
                descriptor.getVersionHigh().convention("1000.0");

                descriptor.getToolchainPlatform().set(project.provider(() -> config.getOperatingSystem().get() + config.getArchitecture().get()));
                toolchainDescriptors.add(descriptor);

                descriptor.getDiscoverers().add(ToolchainDiscoverer.forSystemPath(project, rootExtension, descriptor, name -> {
                    String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
                    return config.getCompilerPrefix().get() + name + exeSuffix;
                }));

                config.getToolchainDescriptor().set(descriptor);
            } else {
                toolchainDescriptors.add(config.getToolchainDescriptor().get());
            }
        });

    }

    public void setSinglePrintPerPlatform() {
        rootExtension.setSinglePrintPerPlatform();
        // ToolchainUtilExtension tcuExt = project.getExtensions().findByType(ToolchainUtilExtension.class);
        // if (tcuExt != null) {
        //     tcuExt.setSkipBinaryToolchainMissingWarning(true);
        // }
    }

    public void withCrossRoboRIO() {
        project.getPluginManager().apply(RoboRioToolchainPlugin.class);
    }

    public void withCrossLinuxArm32() {
        if (!NativePlatforms.desktop.equals(NativePlatforms.linuxarm32)) {
            project.getPluginManager().apply(Arm32ToolchainPlugin.class);
        }
    }

    public void withCrossLinuxArm64() {
        if (!NativePlatforms.desktop.equals(NativePlatforms.linuxarm64)) {
            project.getPluginManager().apply(Arm64ToolchainPlugin.class);
        }
    }

    public NamedDomainObjectContainer<ToolchainDescriptorBase> getToolchainDescriptors() {
        return toolchainDescriptors;
    }

    void toolchainDescriptors(final Action<? super NamedDomainObjectContainer<ToolchainDescriptorBase>> closure) {
        closure.execute(toolchainDescriptors);
    }

    public NamedDomainObjectContainer<CrossCompilerConfiguration> getCrossCompilers() {
        return crossCompilers;
    }

    void crossCompilers(final Action<? super NamedDomainObjectContainer<CrossCompilerConfiguration>> closure) {
        closure.execute(crossCompilers);
    }

    public List<String> getStripExcludeComponentsForPlatform(String platform) {
        return stripExcludeMap.get(platform);
    }

    public void addStripExcludeComponentsForPlatform(String platform, String component) {
        List<String> components = stripExcludeMap.get(platform);
        if (components == null) {
            components = new ArrayList<>();
            components.add(component);
            stripExcludeMap.put(platform, components);
            return;
        }
        components.add(component);
    }

    public void explain(DiagnosticsVisitor visitor) {
        for (ToolchainDescriptorBase desc : toolchainDescriptors) {
            if (desc == null || desc.discover() == null) {
                visitor.node(desc.getName());
                visitor.startChildren();
                visitor.node("Not Found");
                visitor.endChildren();
                continue;
            }
            visitor.node(desc.getName());
            visitor.startChildren();
            visitor.node("Selected: " + desc.discover().getName());
            desc.explain(visitor);
            visitor.endChildren();
        }
    }
}
