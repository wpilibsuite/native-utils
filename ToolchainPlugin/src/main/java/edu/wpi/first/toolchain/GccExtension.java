package edu.wpi.first.toolchain;

import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.Gcc;

public class GccExtension {
    private final Project project;
    public Project getProject() {
        return project;
    }

    private final Gcc toolchain;
    public Gcc getToolchain() {
        return toolchain;
    }
    private final ToolchainDescriptorBase descriptor;
    public ToolchainDescriptorBase getDescriptor() {
        return descriptor;
    }
    private boolean used = false;
    public boolean isUsed() {
        return used;
    }
    public void setUsed(boolean used) {
        this.used = used;
    }
    private final ToolchainDiscoverer discoverer;
    public GccExtension(Gcc toolchain, ToolchainDescriptorBase descriptor, ToolchainDiscoverer discoverer, Project project) {
        this.toolchain = toolchain;
        this.descriptor = descriptor;
        this.discoverer = discoverer;
        this.project = project;
    }
    public ToolchainDiscoverer getDiscoverer() {
        return discoverer;
    }
}
