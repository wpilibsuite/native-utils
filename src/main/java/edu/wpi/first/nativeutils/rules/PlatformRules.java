package edu.wpi.first.nativeutils.rules;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.model.Defaults;
import org.gradle.model.Model;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.configs.ConfigurableCrossPlatformConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.containers.DefaultPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.PlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.impl.DefaultPlatformConfig;
import edu.wpi.first.toolchain.NativePlatforms;

public class PlatformRules extends RuleSource {
  @Model
  PlatformConfigContainer platformConfigs(Instantiator instantiator) {
    return instantiator.newInstance(DefaultPlatformConfigContainer.class, instantiator);
  }

  @Defaults
  void registerFactory(PlatformConfigContainer platforms) {
    NamedDomainObjectFactory<PlatformConfig> platformFactory = new NamedDomainObjectFactory<PlatformConfig>() {

      @Override
      public PlatformConfig create(String name) {
        return new DefaultPlatformConfig(name);
      }
    };

    platforms.registerFactory(PlatformConfig.class, platformFactory);
  }


  private void validatePlatformPath(PlatformConfig config) {
    if (config.getPlatformPath() == null) {
      throw new GradleException("Platform Path cannot be null: " + config.getName());
    }
  }

  @Validate
  void checkPlatformPath(PlatformConfigContainer container) {
    for (PlatformConfig config : container) {
      validatePlatformPath(config);
    }
  }

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

    NamedDomainObjectContainer<ConfigurableCrossPlatformConfig> crossContainer = (NamedDomainObjectContainer<ConfigurableCrossPlatformConfig>)extensionContainer.getByName("configurableCrossCompilers");
    for (ConfigurableCrossPlatformConfig config : crossContainer) {
      NativePlatform configedPlatform = platforms.maybeCreate(config.getOperatingSystem() + config.getArchitecture(), NativePlatform.class);
      configedPlatform.architecture(config.getArchitecture());
      configedPlatform.operatingSystem(config.getOperatingSystem());
    }
  }

  @Validate
  void setupArguments(BinaryContainer binaries, PlatformConfigContainer platforms) {
    for (BinarySpec oBinary : binaries) {
      if (!(oBinary instanceof NativeBinarySpec)) {
        continue;
      }
      NativeBinarySpec binary = (NativeBinarySpec) oBinary;
      String targetName = binary.getTargetPlatform().getName();
      PlatformConfig config = platforms.findByName(targetName);
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
