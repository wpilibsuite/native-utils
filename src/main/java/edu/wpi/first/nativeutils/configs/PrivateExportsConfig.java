package edu.wpi.first.nativeutils.configs;

import org.gradle.api.Named;
import org.gradle.api.file.RegularFileProperty;

public interface PrivateExportsConfig extends Named {
  RegularFileProperty getExportsFile();
}
