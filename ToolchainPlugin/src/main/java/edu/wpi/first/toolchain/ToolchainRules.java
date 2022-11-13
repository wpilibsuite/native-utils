package edu.wpi.first.toolchain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.specs.Spec;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;

public class ToolchainRules extends RuleSource {

    private static final ETLogger logger = ETLoggerFactory.INSTANCE.create("ToolchainRules");

    @Finalize
    void addClangArm(NativeToolChainRegistryInternal toolChainRegistry) {
        toolChainRegistry.all(n -> {
            if (n instanceof Gcc && OperatingSystem.current().equals(OperatingSystem.LINUX)) {
                Gcc gcc = (Gcc)n;
                if (NativePlatforms.desktop.equals(NativePlatforms.linuxarm32) || NativePlatforms.desktop.equals(NativePlatforms.linuxarm64)) {
                    gcc.setTargets();
                    gcc.target(NativePlatforms.desktop);
                }
            }
            if (n instanceof Clang && OperatingSystem.current().equals(OperatingSystem.MAC_OS)) {
                Clang gcc = (Clang)n;
                gcc.setTargets();
                gcc.target("osxuniversal", gccToolChain -> {
                    Action<List<String>> m64args = new Action<List<String>>() {
                        @Override
                        public void execute(List<String> args) {
                            args.add("-arch");
                            args.add("arm64");
                            args.add("-arch");
                            args.add("x86_64");
                        }
                    };
                    gccToolChain.getCppCompiler().withArguments(m64args);
                    gccToolChain.getcCompiler().withArguments(m64args);
                    gccToolChain.getObjcCompiler().withArguments(m64args);
                    gccToolChain.getObjcppCompiler().withArguments(m64args);
                    gccToolChain.getLinker().withArguments(m64args);
                    gccToolChain.getAssembler().withArguments(m64args);
                });
            }
        });
    }

    @Defaults
    void addDefaultToolchains(NativeToolChainRegistryInternal toolChainRegistry,
            ExtensionContainer extContainer) {

        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        ext.getToolchainDescriptors().all(desc -> {
            logger.info("Descriptor Register: " + desc.getName());

            toolChainRegistry.registerDefaultToolChain(desc.getGccName(), Gcc.class);

            toolChainRegistry.containerWithType(Gcc.class).configureEach(tc -> {
                if (tc.getName().equals(desc.getGccName())) {
                    ToolchainDiscoverer discoverer = desc.discover();
                    GccExtension gccExt = new GccExtension(tc, desc, discoverer);
                    ext.getGccExtensionMap().put(tc, gccExt);

                    tc.setTargets(desc.getToolchainPlatform().get());

                    if (discoverer != null) {
                        tc.eachPlatform(toolchain -> {
                            toolchain.getcCompiler().setExecutable(discoverer.toolName("gcc"));
                            toolchain.getCppCompiler().setExecutable(discoverer.toolName("g++"));
                            toolchain.getLinker().setExecutable(discoverer.toolName("g++"));
                            toolchain.getAssembler().setExecutable(discoverer.toolName("as"));
                            toolchain.getStaticLibArchiver().setExecutable(discoverer.toolName("ar"));
                        });

                        if (discoverer.sysroot().isPresent())
                            tc.path(discoverer.binDir().get());
                    } else {
                        ext.getRootExtension().addMissingToolchain(gccExt);
                        tc.path("NOTOOLCHAINPATH");
                    }
                }
            });
        });


    }

    @Mutate
    void addBuildTypes(BuildTypeContainer buildTypes, final ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerDebugBuildType) {
            buildTypes.maybeCreate("debug");
        }

        if (ext.registerReleaseBuildType) {
            buildTypes.maybeCreate("release");
        }
    }

    @Validate
    void disableCrossTests(BinaryContainer binaries) {
        String currentTarget = NativePlatforms.desktop;

        Action<RunTestExecutable> eachTaskAction = new Action<RunTestExecutable>() {

            Spec<Task> forceTaskToNotRunSpec = new Spec<Task>() {
                @Override
                public boolean isSatisfiedBy(Task arg0) {
                    return false;
                }
            };

            @Override
            public void execute(RunTestExecutable test) {
                test.onlyIf(forceTaskToNotRunSpec);
            }

        };

        for (GoogleTestTestSuiteBinarySpec binary : binaries.withType(GoogleTestTestSuiteBinarySpec.class)) {
            if (!binary.getTargetPlatform().getName().equals(currentTarget)) {
                binary.getTasks().withType(RunTestExecutable.class, eachTaskAction);
            }
        }
    }

    @Mutate
    void addDefaultPlatforms(final ExtensionContainer extContainer, final PlatformContainer platforms) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerPlatforms) {
            NativePlatform desktop = platforms.maybeCreate(NativePlatforms.desktop, NativePlatform.class);
            desktop.architecture(NativePlatforms.desktopArch().replaceAll("-", "_"));

            for (CrossCompilerConfiguration config : ext.getCrossCompilers()) {
                if (config.getName().equals(NativePlatforms.desktop)) {
                    continue;
                }
                NativePlatform configedPlatform = platforms.maybeCreate(config.getName(), NativePlatform.class);
                configedPlatform.architecture(config.getArchitecture().get());
                configedPlatform.operatingSystem(config.getOperatingSystem().get());
            }
        }
    }

    @Validate
    void checkEnabledToolchains(final BinaryContainer binaries, final NativeToolChainRegistry toolChains, final ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);
        // Map of platform to toolchains
        Map<String, GccExtension> gccToolChains = new HashMap<>();
        for (NativeToolChain toolChain : toolChains) {
            GccExtension gccExt = ext.getGccExtensionMap().getOrDefault(toolChain, null);
            if (gccExt != null) {
                gccToolChains.put(gccExt.getDescriptor().getToolchainPlatform().get(), gccExt);
            }
        }

        for (BinarySpec oBinary : binaries) {
            if (!(oBinary instanceof NativeBinarySpec)) {
                continue;
            }
            NativeBinarySpec binary = (NativeBinarySpec) oBinary;
            GccExtension chain = gccToolChains.getOrDefault(binary.getTargetPlatform().getName(), null);
            // Can't use getToolChain, as that is invalid for unknown platforms
            if (chain != null) {
                chain.setUsed(true);
            }
        }
    }

    public static OrderedStripTask configureOrderedStrip(AbstractLinkTask link, GccExtension gcc, NativeBinarySpec binary) {
        ExtensionAware linkExt = (ExtensionAware)link;
        OrderedStripTask strip = linkExt.getExtensions().findByType(OrderedStripTask.class);
        if (strip == null) {
            Project project = link.getProject();
            ToolchainExtension tcExt = project.getExtensions().findByType(ToolchainExtension.class);

            if (tcExt == null) {
                return null;
            }

            strip = linkExt.getExtensions().create("orderedStrip", OrderedStripTask.class, tcExt, binary, link, gcc, project);
            link.doLast(strip);
        }
        return strip;
    }

    @BinaryTasks
    void createNativeStripTasks(final ModelMap<Task> tasks, NativeBinarySpec binary, final ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);
        NativeToolChain tc = binary.getToolChain();
        GccExtension gccExt = ext.getGccExtensionMap().getOrDefault(tc, null);
        if (gccExt == null) {
            return;
        }

        binary.getTasks().withType(AbstractLinkTask.class, link -> {
            configureOrderedStrip(link, gccExt, binary).setPerformDebugStrip(true);
        });
    }
}
