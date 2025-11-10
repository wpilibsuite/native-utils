package edu.wpi.first.toolchain;

import java.util.List;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.internal.logging.text.DiagnosticsVisitor;

public interface ToolchainDescriptorBase extends Named {

  public Provider<String> getVersionLow();
  public Provider<String> getVersionHigh();

  public Property<String> getToolchainPlatform();

  public List<ToolchainDiscovererProperty> getDiscoverers();

  public DomainObjectSet<AbstractToolchainInstaller> getInstallers();

  public void explain(DiagnosticsVisitor visitor);

  public ToolchainDiscoverer discover();

  public AbstractToolchainInstaller getInstaller();

  public String getGccName();

  public String getInstallTaskName();

  public Property<Boolean> getOptional();
}
