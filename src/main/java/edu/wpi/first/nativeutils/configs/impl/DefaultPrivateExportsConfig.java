package edu.wpi.first.nativeutils.configs.impl;

import javax.inject.Inject;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

import edu.wpi.first.nativeutils.configs.PrivateExportsConfig;

public class DefaultPrivateExportsConfig implements PrivateExportsConfig {
  private RegularFileProperty exportsFile;

  private String name;

  @Inject
  public DefaultPrivateExportsConfig(String name, ObjectFactory factory) {
    exportsFile = factory.fileProperty();
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
}
