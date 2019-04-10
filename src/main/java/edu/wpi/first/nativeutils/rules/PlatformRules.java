package edu.wpi.first.nativeutils.rules;

import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.CrossCompilerConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.toolchain.NativePlatforms;

public class PlatformRules extends RuleSource {

  @Mutate
  void addBuildTypes(BuildTypeContainer buildTypes) {
    buildTypes.maybeCreate("release");
    buildTypes.maybeCreate("debug");
  }

  @Mutate
  void addExtraPlatforms(final ExtensionContainer extensionContainer, final PlatformContainer platforms) {
    NativePlatform roborio = platforms.maybeCreate(NativePlatforms.roborio, NativePlatform.class);
    roborio.architecture("arm");
    roborio.operatingSystem("linux");

    NativePlatform raspbian = platforms.maybeCreate(NativePlatforms.raspbian, NativePlatform.class);
    raspbian.architecture("arm");
    raspbian.operatingSystem("linux");

    NativePlatform desktop = platforms.maybeCreate(NativePlatforms.desktop, NativePlatform.class);
    desktop.architecture(NativePlatforms.desktopArch().replaceAll("-", "_"));

    platforms.maybeCreate("windowsx86", NativePlatform.class);

    NativeUtilsExtension extension = extensionContainer.getByType(NativeUtilsExtension.class);
    for (CrossCompilerConfig config : extension.getConfigurableCrossCompilers()) {
      NativePlatform configedPlatform = platforms.maybeCreate(config.getName(), NativePlatform.class);
      configedPlatform.architecture(config.getArchitecture());
      configedPlatform.operatingSystem(config.getOperatingSystem());
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
