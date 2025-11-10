package org.wpilib.nativeutils.platforms;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.PlatformContainer;

import org.wpilib.nativeutils.NativeUtilsExtension;

public class PlatformRules extends RuleSource {
  @Validate
  void setuPlatforms(ComponentSpecContainer components, PlatformContainer platforms, ExtensionContainer extensions, BuildTypeContainer buildTypes) {
    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    extension.addPlatformsToConfigure(platforms);

    extension.addPlatformsAndBuildTypes(platforms, buildTypes);
  }
}
