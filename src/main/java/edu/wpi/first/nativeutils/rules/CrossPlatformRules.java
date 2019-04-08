package edu.wpi.first.nativeutils.rules;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.model.Defaults;
import org.gradle.model.Model;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;

import edu.wpi.first.nativeutils.configs.CrossPlatformConfig;
import edu.wpi.first.nativeutils.configs.containers.CrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.containers.DefaultCrossPlatformConfigContainer;
import edu.wpi.first.nativeutils.configs.impl.DefaultCrossPlatformConfig;
import edu.wpi.first.toolchain.GccToolChain;

public class CrossPlatformRules extends RuleSource {
  @Model
  CrossPlatformConfigContainer crossPlatformConfigs(Instantiator instantiator) {
      return instantiator.newInstance(DefaultCrossPlatformConfigContainer.class, instantiator);
  }

  @Defaults
  void registerFactory(CrossPlatformConfigContainer platforms) {
    NamedDomainObjectFactory<CrossPlatformConfig> platformFactory = new NamedDomainObjectFactory<CrossPlatformConfig>() {

      @Override
      public CrossPlatformConfig create(String name) {
        return new DefaultCrossPlatformConfig(name);
      }
    };

    platforms.registerFactory(CrossPlatformConfig.class, platformFactory);
  }

  @Validate
  void validatePlatformSet(CrossPlatformConfigContainer configContainer) {
    for (CrossPlatformConfig config : configContainer) {
      if (config.getToolChain() == null) {
        throw new AssertionError(config.getName() + " getToolChain() == null");
      }
      if (config.getToolChain().equals(GccToolChain.class)) {
        throw new AssertionError(config.getName() + " getToolChain().equals(GccToolChain.class)");
      }
    }
  }
}
