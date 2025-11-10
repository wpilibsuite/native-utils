package org.wpilib.toolchain;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.internal.logging.text.DiagnosticsVisitor;

public class ToolchainDescriptor implements ToolchainDescriptorBase {

    private final String name;
    private final String gccName;
    private final Property<String> versionHigh;

    @Override
    public Property<String> getVersionHigh() {
        return versionHigh;
    }

    private final Property<String> versionLow;

    @Override
    public Property<String> getVersionLow() {
        return versionLow;
    }

    private final Property<String> platform;
    private final Property<Boolean> optional;
    private final List<ToolchainDiscovererProperty> discoverers;
    private final DomainObjectSet<AbstractToolchainInstaller> installers;

    public ToolchainDescriptor(Project project, String name, String gccName, Property<Boolean> optional) {
        this.name = name;
        this.platform = project.getObjects().property(String.class);
        this.versionLow = project.getObjects().property(String.class);
        this.versionHigh = project.getObjects().property(String.class);
        this.optional = optional;
        this.gccName = gccName;
        this.discoverers = new ArrayList<>();
        this.installers = project.getObjects().domainObjectSet(AbstractToolchainInstaller.class);
    }

    @Override
    public Property<String> getToolchainPlatform() {
        return platform;
    }

    @Override
    public List<ToolchainDiscovererProperty> getDiscoverers() {
        return discoverers;
    }

    @Override
    public DomainObjectSet<AbstractToolchainInstaller> getInstallers() {
        return installers;
    }

    @Override
    public ToolchainDiscoverer discover() {
        return discoverers.stream().flatMap(x -> x.getDiscovererList().stream()).filter(ToolchainDiscoverer::valid).findFirst().orElse(null);
    }

    @Override
    public AbstractToolchainInstaller getInstaller() {
        return installers.stream().filter(AbstractToolchainInstaller::installable).findFirst().orElse(null);
    }

    @Override
    public void explain(DiagnosticsVisitor visitor) {
        for (ToolchainDiscovererProperty pdiscoverer : discoverers) {
            for (ToolchainDiscoverer discoverer : pdiscoverer.getDiscovererList()) {
                visitor.node(discoverer.getName());
                visitor.startChildren();
                discoverer.explain(visitor);
                visitor.endChildren();
            }
        }
    }

    @Override
    public Property<Boolean> getOptional() {
        return optional;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGccName() {
        return gccName;
    }

    @Override
    public String getInstallTaskName() {
        return "install" + capitalize(getName()) + "Toolchain";
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
