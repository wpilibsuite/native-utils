package edu.wpi.first.toolchain;

import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;

public abstract class GccToolChain extends AbstractGccCompatibleToolChain {

    private Project project;
    private ToolchainDescriptorBase descriptor;
    private ToolchainDiscoverer discoverer;
    private boolean isUsed;

    public GccToolChain(ToolchainOptions options) {
        super(options.name,
                options.buildOperationExecutor,
                options.operatingSystem,
                options.fileResolver,
                options.execActionFactory,
                options.compilerOutputFileNamingSchemeFactory,
                options.metaDataProviderFactory.gcc(),
                options.systemLibraryDiscovery,
                options.instantiator,
                options.workerLeaseService);

        this.project = options.project;
        this.descriptor = options.descriptor;
        this.discoverer = descriptor.discover();

        setTargets(descriptor.getToolchainPlatform().get());

        if (discoverer != null) {
            eachPlatform(toolchain -> {
                toolchain.getcCompiler().setExecutable(discoverer.toolName("gcc"));
                toolchain.getCppCompiler().setExecutable(discoverer.toolName("g++"));
                toolchain.getLinker().setExecutable(discoverer.toolName("g++"));
                toolchain.getAssembler().setExecutable(discoverer.toolName("as"));
                toolchain.getStaticLibArchiver().setExecutable(discoverer.toolName("ar"));
            });

            if (discoverer.sysroot().isPresent())
                path(discoverer.binDir().get());
        } else {
            project.getExtensions().getByType(ToolchainExtension.class).getRootExtension().addMissingToolchain(this);
            path("NOTOOLCHAINPATH");
        }
    }

    public Project getProject() {
        return project;
    }

    public ToolchainDescriptorBase getDescriptor() {
        return descriptor;
    }

    public ToolchainDiscoverer getDiscoverer() {
        return discoverer;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isUsed() {
        return isUsed;
    }
}
