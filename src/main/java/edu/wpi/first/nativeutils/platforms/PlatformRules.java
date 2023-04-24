package edu.wpi.first.nativeutils.platforms;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.PlatformContainer;

public class PlatformRules extends RuleSource {
  @Validate
  void setuPlatforms(
      ComponentSpecContainer components,
      PlatformContainer platforms,
      ExtensionContainer extensions) {
    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    extension.addPlatformsToConfigure(platforms);
  }
}
