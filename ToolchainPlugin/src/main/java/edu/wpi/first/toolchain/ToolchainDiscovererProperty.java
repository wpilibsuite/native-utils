package edu.wpi.first.toolchain;

import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;

public abstract class ToolchainDiscovererProperty implements Named {
  private final String name;
  private List<ToolchainDiscoverer> realizedDiscoverers;

  public String getName() {
    return name;
  }

  @Input
  public abstract ListProperty<ToolchainDiscoverer> getDiscoverers();

  public List<ToolchainDiscoverer> getDiscovererList() {
    if (realizedDiscoverers != null) {
      return realizedDiscoverers;
    }
    realizedDiscoverers = getDiscoverers().get();
    return realizedDiscoverers;
  }

  @Inject
  public ToolchainDiscovererProperty(String name) {
    this.name = name;
  }
}
