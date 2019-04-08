package edu.wpi.first.nativeutils.rules;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.model.Defaults;
import org.gradle.model.Model;
import org.gradle.model.RuleSource;

import edu.wpi.first.nativeutils.configs.ConfigurableCrossPlatformConfig;
import edu.wpi.first.nativeutils.configs.containers.ConfigurableCrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.DefaultConfigurableCrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.impl.DefaultConfigurableCrossPlatformConfig;

public class NativePlatformRules extends RuleSource {
  @Model
  ConfigurableCrossPlatformConfigContainer configurableCrossPlatformConfigs(Instantiator instantiator) {
      return instantiator.newInstance(DefaultConfigurableCrossPlatformConfigContainer.class, instantiator);
  }

  @Defaults
  void registerFactory(ConfigurableCrossPlatformConfigContainer platforms) {
    NamedDomainObjectFactory<ConfigurableCrossPlatformConfig> platformFactory = new NamedDomainObjectFactory<ConfigurableCrossPlatformConfig>() {

      @Override
      public ConfigurableCrossPlatformConfig create(String name) {
        return new DefaultConfigurableCrossPlatformConfig(name);
      }
    };

    platforms.registerFactory(ConfigurableCrossPlatformConfig.class, platformFactory);
  }
}
