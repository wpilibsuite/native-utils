package org.wpilib.toolchain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.internal.logging.text.DiagnosticsVisitor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.process.ExecOperations;
import org.gradle.api.model.ObjectFactory;

import org.wpilib.toolchain.arm64.Arm64ToolchainPlugin;
import org.wpilib.toolchain.systemcore.SystemCoreToolchainPlugin;
import org.wpilib.toolchain.configurable.CrossCompilerConfiguration;

public class ToolchainExtension {
    private final NamedDomainObjectContainer<CrossCompilerConfiguration> crossCompilers;
    private final NamedDomainObjectContainer<ToolchainDescriptorBase> toolchainDescriptors;
    private final Map<String, List<String>> stripExcludeMap = new HashMap<>();
    private final Map<Gcc, GccExtension> gccExtensionMap = new HashMap<>();

    public Map<Gcc, GccExtension> getGccExtensionMap() {
        return gccExtensionMap;
    }

    private Project project;

    public Project getProject() {
        return project;
    }

    public boolean registerPlatforms = true;
    public boolean registerReleaseBuildType = true;
    public boolean registerDebugBuildType = true;
    private final ToolchainGraphBuildService rootExtension;

    public ToolchainGraphBuildService getToolchainGraphService() {
        return rootExtension;
    }

    @Inject
    public ToolchainExtension(Project project, ToolchainGraphBuildService rootExtension, ExecOperations operations, ObjectFactory objectFactory) {
        this.project = project;
        this.rootExtension = rootExtension;

        crossCompilers = objectFactory.domainObjectContainer(CrossCompilerConfiguration.class, name -> {
            return project.getObjects().newInstance(CrossCompilerConfiguration.class, name);
        });

        toolchainDescriptors = objectFactory.domainObjectContainer(ToolchainDescriptorBase.class);

        NativeToolChainRegistryInternal toolChainRegistry = (NativeToolChainRegistryInternal)project.getExtensions().getByType(NativeToolChainRegistry.class);

        toolChainRegistry.containerWithType(Gcc.class).configureEach(tc -> {
            ToolchainDescriptorBase desc = null;
            for (ToolchainDescriptorBase base : toolchainDescriptors) {
                if (base.getGccName().equals(tc.getName())) {
                    desc = base;
                    break;
                }
            }
            if (desc == null) {
                return;
            }
            ToolchainDiscoverer discoverer = desc.discover();
            GccExtension gccExt = new GccExtension(tc, desc, discoverer, this.getProject());
            this.getGccExtensionMap().put(tc, gccExt);

            tc.setTargets(desc.getToolchainPlatform().get());

            if (discoverer != null) {
                tc.eachPlatform(toolchain -> {
                    toolchain.getcCompiler().setExecutable(discoverer.toolName("gcc"));
                    toolchain.getCppCompiler().setExecutable(discoverer.toolName("g++"));
                    toolchain.getLinker().setExecutable(discoverer.toolName("g++"));
                    toolchain.getAssembler().setExecutable(discoverer.toolName("gcc"));
                    toolchain.getStaticLibArchiver().setExecutable(discoverer.toolName("ar"));
                });

                if (discoverer.sysroot().isPresent())
                    tc.path(discoverer.binDir().get());
            } else {
                this.getToolchainGraphService().addMissingToolchain(gccExt);
                tc.path("NOTOOLCHAINPATH");
            }
        });

        crossCompilers.all(config -> {
            if (!config.getToolchainDescriptor().isPresent()) {
                config.getOptional().convention(true);
                ToolchainDescriptor descriptor = new ToolchainDescriptor(
                        project,
                        config.getName(),
                        config.getName() + "ConfiguredGcc",
                        config.getOptional());

                descriptor.getVersionLow().convention("0.0");
                descriptor.getVersionHigh().convention("1000.0");

                descriptor.getToolchainPlatform().set(
                        project.provider(() -> config.getOperatingSystem().get() + config.getArchitecture().get()));
                toolchainDescriptors.add(descriptor);

                descriptor.getDiscoverers()
                        .add(ToolchainDiscoverer.forSystemPath(project, rootExtension, descriptor, name -> {
                            String exeSuffix = OperatingSystem.current().isWindows() ? ".exe" : "";
                            return config.getCompilerPrefix().get() + name + exeSuffix;
                        }, operations));

                config.getToolchainDescriptor().set(descriptor);
            } else {
                toolchainDescriptors.add(config.getToolchainDescriptor().get());
            }
            toolChainRegistry.registerDefaultToolChain(config.getToolchainDescriptor().get().getGccName(), Gcc.class);
        });

    }

    public void setSinglePrintPerPlatform() {
        rootExtension.setSinglePrintPerPlatform();
        // ToolchainUtilExtension tcuExt =
        // project.getExtensions().findByType(ToolchainUtilExtension.class);
        // if (tcuExt != null) {
        // tcuExt.setSkipBinaryToolchainMissingWarning(true);
        // }
    }

    public void withCrossLinuxArm64() {
        if (!NativePlatforms.desktop.equals(NativePlatforms.linuxarm64)) {
            project.getPluginManager().apply(Arm64ToolchainPlugin.class);
        }
    }

    public void withCrossSystemCore() {
        project.getPluginManager().apply(SystemCoreToolchainPlugin.class);
    }

    private boolean removeInvalidWindowsToolchains = true;

    public void setRemoveInvalidWindowsToolchains(boolean remove) {
        this.removeInvalidWindowsToolchains = remove;
    }

    public boolean isRemoveInvalidWindowsToolchains() {
        return this.removeInvalidWindowsToolchains;
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
