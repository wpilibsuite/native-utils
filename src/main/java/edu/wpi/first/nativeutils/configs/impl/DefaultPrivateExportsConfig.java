package edu.wpi.first.nativeutils.configs.impl;

import javax.inject.Inject;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import edu.wpi.first.nativeutils.configs.PrivateExportsConfig;

public class DefaultPrivateExportsConfig implements PrivateExportsConfig {
  private final RegularFileProperty exportsFile;
  private final Property<Boolean> performStripAllSymbols;

  private String name;

  @Inject
  public DefaultPrivateExportsConfig(String name, ObjectFactory factory) {
    exportsFile = factory.fileProperty();
    performStripAllSymbols = factory.property(Boolean.class);
    performStripAllSymbols.set(false);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public RegularFileProperty getExportsFile() {
    return exportsFile;
  }

  @Override
  public Property<Boolean> getPerformStripAllSymbols() {
    return performStripAllSymbols;
  }
}
