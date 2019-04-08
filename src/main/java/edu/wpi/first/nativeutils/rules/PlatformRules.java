package edu.wpi.first.nativeutils.rules;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.model.Defaults;
import org.gradle.model.Model;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;

import edu.wpi.first.nativeutils.configs.NativePlatformConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.containers.ConfigurableCrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.CrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.DefaultNativePlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.NativePlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.impl.DefaultNativePlatformConfig;

public class PlatformRules extends RuleSource {
  @Model
  NativePlatformConfigContainer nativePlatformConfigs(Instantiator instantiator) {
      return instantiator.newInstance(DefaultNativePlatformConfigContainer.class, instantiator);
  }

  @Defaults
  void registerFactory(NativePlatformConfigContainer platforms) {
    NamedDomainObjectFactory<NativePlatformConfig> platformFactory = new NamedDomainObjectFactory<NativePlatformConfig>() {

      @Override
      public NativePlatformConfig create(String name) {
        return new DefaultNativePlatformConfig(name);
      }
    };

    platforms.registerFactory(NativePlatformConfig.class, platformFactory);
  }

  private void validatePlatformPath(PlatformConfig config) {
    if (config.getPlatformPath() == null) {
      throw new GradleException("Platform Path cannot be null: " + config.getName());
    }
  }

  @Validate
  void checkNativePlatformPath(NativePlatformConfigContainer container) {
    for (PlatformConfig config : container) {
      validatePlatformPath(config);
    }
  }

  @Validate
  void checkcCrossPlatformPath(ConfigurableCrossPlatformConfigContainer container) {
    for (PlatformConfig config : container) {
      validatePlatformPath(config);
    }
  }

  @Validate
  void checkCrossPlatformPath(CrossPlatformConfigContainer container) {
    for (PlatformConfig config : container) {
      validatePlatformPath(config);
    }
  }

  @Mutate
  void addBuildTypes(BuildTypeContainer buildTypes) {
      buildTypes.maybeCreate("release");
      buildTypes.maybeCreate("debug");
  }

  @Validate
  void setupArguments(BinaryContainer binaries, NativePlatformConfigContainer nativePlatforms, ConfigurableCrossPlatformConfigContainer configurableCrossPlatforms, CrossPlatformConfigContainer crossPlatforms) {
    // Build a common map
    Map<String, PlatformConfig> configMap = new HashMap<>();
    configMap.putAll(nativePlatforms.getAsMap());
    configMap.putAll(configurableCrossPlatforms.getAsMap());
    configMap.putAll(crossPlatforms.getAsMap());
    for (BinarySpec oBinary : binaries) {
      if (!(oBinary instanceof NativeBinarySpec)) {
        continue;
      }
      NativeBinarySpec binary = (NativeBinarySpec)oBinary;
      String targetName = binary.getTargetPlatform().getName();
      PlatformConfig config = configMap.get(targetName);
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
