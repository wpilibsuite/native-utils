package edu.wpi.first.toolchain;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.internal.logging.text.DiagnosticsVisitor;

public interface ToolchainDescriptorBase extends Named {

  public void setToolchainPlatforms(String... platforms);

  public NamedDomainObjectSet<ToolchainDiscoverer> getDiscoverers();

  public DomainObjectSet<AbstractToolchainInstaller> getInstallers();

  public void explain(DiagnosticsVisitor visitor);

  public ToolchainDiscoverer discover();

  public AbstractToolchainInstaller getInstaller();

  public String getToolchainName();

  public String getInstallTaskName();

  public boolean isOptional();

  public void setOptional(boolean optional);

  public String[] getToolchainPlatforms();

  public ToolchainRegistrarBase getRegistrar();
}
