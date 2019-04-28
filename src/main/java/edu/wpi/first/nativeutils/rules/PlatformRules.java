package edu.wpi.first.nativeutils.rules;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class PlatformRules extends RuleSource {
  @Validate
  void setuPlatforms(ComponentSpecContainer components, PlatformContainer platforms, ExtensionContainer extensions) {
    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    for (Platform platform : platforms) {
      extension.addPlatformToConfigure(platform.getName());
    }
  }

  @Validate
  void setupArguments(BinaryContainer binaries, ExtensionContainer extensionContainer) {
    NativeUtilsExtension extension = extensionContainer.getByType(NativeUtilsExtension.class);
    for (BinarySpec oBinary : binaries) {
      if (!(oBinary instanceof NativeBinarySpec)) {
        continue;
      }
      NativeBinarySpec binary = (NativeBinarySpec) oBinary;
      String targetName = binary.getTargetPlatform().getName();
      PlatformConfig config = extension.getPlatformConfigs().findByName(targetName);
      if (config == null) {
        continue;
      }

      boolean isDebug = binary.getBuildType().getName().contains("debug");
      config.getCppCompiler().apply(binary.getCppCompiler(), isDebug);
      config.getLinker().apply(binary.getLinker(), isDebug);
      config.getcCompiler().apply(binary.getcCompiler(), isDebug);
      config.getAssembler().apply(binary.getAssembler(), isDebug);
      config.getObjcppCompiler().apply(binary.getObjcppCompiler(), isDebug);
      config.getObjcCompiler().apply(binary.getObjcCompiler(), isDebug);
    }
  }
}
