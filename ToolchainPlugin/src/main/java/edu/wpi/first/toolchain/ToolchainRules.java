package edu.wpi.first.toolchain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.clang.ClangToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.BinaryTasks;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.process.internal.ExecActionFactory;

import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;

public class ToolchainRules extends RuleSource {

    private static final ETLogger logger = ETLoggerFactory.INSTANCE.create("ToolchainRules");

    @Finalize
    void addClangArm(NativeToolChainRegistryInternal toolChainRegistry) {
        toolChainRegistry.all(n -> {
            if (n instanceof ClangToolChain && OperatingSystem.current().equals(OperatingSystem.MAC_OS)) {
                AbstractGccCompatibleToolChain gcc = (AbstractGccCompatibleToolChain)n;
                gcc.setTargets();
                gcc.target("osxarm64", gccToolChain -> {
                    Action<List<String>> m64args = new Action<List<String>>() {
                        @Override
                        public void execute(List<String> args) {
                            args.add("-arch");
                            args.add("arm64");
                        }
                    };
                    gccToolChain.getCppCompiler().withArguments(m64args);
                    gccToolChain.getcCompiler().withArguments(m64args);
                    gccToolChain.getObjcCompiler().withArguments(m64args);
                    gccToolChain.getObjcppCompiler().withArguments(m64args);
                    gccToolChain.getLinker().withArguments(m64args);
                    gccToolChain.getAssembler().withArguments(m64args);
                });
                gcc.target("osxx86-64", gccToolChain -> {
                    Action<List<String>> m64args = new Action<List<String>>() {
                        @Override
                        public void execute(List<String> args) {
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
    void addDefaultToolchains(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry,
            ExtensionContainer extContainer) {
        final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
        final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
        final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry
                .get(CompilerOutputFileNamingSchemeFactory.class);
        final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
        final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
        final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry
                .get(CompilerMetaDataProviderFactory.class);
        final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);
        final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery.class);

        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        ext.getToolchainDescriptors().all(desc -> {
            logger.info("Descriptor Register: " + desc.getName());
            ToolchainOptions options = new ToolchainOptions(instantiator, buildOperationExecutor,
                    OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory,
                    metaDataProviderFactory, workerLeaseService, standardLibraryDiscovery);
            options.descriptor = desc;

            desc.getRegistrar().register(options, toolChainRegistry, instantiator);
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
    void disableCrossTests(BinaryContainer binaries, ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        for (GoogleTestTestSuiteBinarySpec binary : binaries.withType(GoogleTestTestSuiteBinarySpec.class)) {
            if (ext.getCrossCompilers().findByName(binary.getTargetPlatform().getName()) != null) {
                for (RunTestExecutable runExe : binary.getTasks().withType(RunTestExecutable.class)) {
                    runExe.onlyIf(t -> {
                        return false;
                    });
                }
            }
        }
    }

    @Mutate
    void addDefaultPlatforms(final ExtensionContainer extContainer, final PlatformContainer platforms) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerPlatforms) {
            NativePlatform desktop = platforms.maybeCreate(NativePlatforms.desktop, NativePlatform.class);
            desktop.architecture(NativePlatforms.desktopArch().replaceAll("-", "_"));

            NativePlatforms.PlatformArchPair[] extraPlatforms = NativePlatforms.desktopExtraPlatforms();

            for (NativePlatforms.PlatformArchPair platform : extraPlatforms) {
                NativePlatform toCreate = platforms.maybeCreate(platform.platformName, NativePlatform.class);
                toCreate.architecture(platform.arch);
            }

            for (CrossCompilerConfiguration config : ext.getCrossCompilers()) {
                if (config.getName().equals(NativePlatforms.desktop)) {
                    continue;
                }
                NativePlatform configedPlatform = platforms.maybeCreate(config.getName(), NativePlatform.class);
                configedPlatform.architecture(config.getArchitecture());
                configedPlatform.operatingSystem(config.getOperatingSystem());
            }
        }
    }

    @Validate
    void checkEnabledToolchains(final BinaryContainer binaries, final NativeToolChainRegistry toolChains) {
        // Map of platform to toolchains
        Map<String, GccToolChain> gccToolChains = new HashMap<>();
        for (NativeToolChain toolChain : toolChains) {
            if (toolChain instanceof GccToolChain) {
                GccToolChain gccToolChain = (GccToolChain) toolChain;
                for (String name : gccToolChain.getDescriptor().getToolchainPlatforms()) {
                    gccToolChains.put(name, gccToolChain);
                }

            }
        }

        for (BinarySpec oBinary : binaries) {
            if (!(oBinary instanceof NativeBinarySpec)) {
                continue;
            }
            NativeBinarySpec binary = (NativeBinarySpec) oBinary;
            GccToolChain chain = gccToolChains.getOrDefault(binary.getTargetPlatform().getName(), null);
            // Can't use getToolChain, as that is invalid for unknown platforms
            if (chain != null) {
                chain.setUsed(true);
            }
        }
    }

    public static OrderedStripTask configureOrderedStrip(AbstractLinkTask link, GccToolChain gcc, NativeBinarySpec binary) {
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

        GccToolChain gcc = null;
            if (binary.getToolChain() instanceof GccToolChain) {
                gcc = (GccToolChain) binary.getToolChain();
            } else {
                return;
            }

        GccToolChain gccFinal = gcc;

        binary.getTasks().withType(AbstractLinkTask.class, link -> {
            configureOrderedStrip(link, gccFinal, binary).setPerformDebugStrip(true);
        });
    }
}
