package edu.wpi.first.toolchain;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.internal.logging.text.DiagnosticsVisitor;

public class ToolchainDescriptor<T extends GccToolChain> implements ToolchainDescriptorBase {

    private final String name;
    private final String toolchainName;
    private String[] platforms;
    private final Property<Boolean> optional;
    private final NamedDomainObjectSet<ToolchainDiscoverer> discoverers;
    private final DomainObjectSet<AbstractToolchainInstaller> installers;

    private final ToolchainRegistrar<T> registrar;

    public ToolchainDescriptor(Project project, String name, String toolchainName, ToolchainRegistrar<T> registrar, Property<Boolean> optional) {
        this.name = name;
        this.platforms = null;
        this.optional = optional;
        this.registrar = registrar;
        this.toolchainName = toolchainName;
        this.discoverers = project.getObjects().namedDomainObjectSet(ToolchainDiscoverer.class);
        this.installers = project.getObjects().domainObjectSet(AbstractToolchainInstaller.class);
    }

    @Override
    public void setToolchainPlatforms(String... platforms) {
        this.platforms = platforms;
    }

    @Override
    public NamedDomainObjectSet<ToolchainDiscoverer> getDiscoverers() {
        return discoverers;
    }

    @Override
    public DomainObjectSet<AbstractToolchainInstaller> getInstallers() {
        return installers;
    }

    @Override
    public ToolchainDiscoverer discover() {
        return discoverers.stream().filter(ToolchainDiscoverer::valid).findFirst().orElse(null);
    }

    @Override
    public AbstractToolchainInstaller getInstaller() {
        return installers.stream().filter(AbstractToolchainInstaller::installable).findFirst().orElse(null);
    }

    @Override
    public void explain(DiagnosticsVisitor visitor) {
        for (ToolchainDiscoverer discoverer : discoverers) {
            visitor.node(discoverer.getName());
            visitor.startChildren();
            discoverer.explain(visitor);
            visitor.endChildren();
        }
    }

    @Override
    public Property<Boolean> getOptional() {
        return optional;
    }

    @Override
    public String[] getToolchainPlatforms() {
        return platforms;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getToolchainName() {
        return toolchainName;
    }

    @Override
    public String getInstallTaskName() {
        return "install" + capitalize(getName()) + "Toolchain";
    }

    @Override
    public ToolchainRegistrar<T> getRegistrar() {
        return registrar;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
