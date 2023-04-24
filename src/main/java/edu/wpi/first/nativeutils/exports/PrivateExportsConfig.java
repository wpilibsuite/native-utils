package edu.wpi.first.nativeutils.exports;

import org.gradle.api.Named;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface PrivateExportsConfig extends Named {
  RegularFileProperty getExportsFile();

  Property<Boolean> getPerformStripAllSymbols();
}
