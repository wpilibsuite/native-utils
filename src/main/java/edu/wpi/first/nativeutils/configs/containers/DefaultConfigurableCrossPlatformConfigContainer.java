package edu.wpi.first.nativeutils.configs.containers;

import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

import edu.wpi.first.nativeutils.configs.ConfigurableCrossPlatformConfig;

public class DefaultConfigurableCrossPlatformConfigContainer extends DefaultPolymorphicDomainObjectContainer<ConfigurableCrossPlatformConfig>
    implements ConfigurableCrossPlatformConfigContainer {

    public DefaultConfigurableCrossPlatformConfigContainer(Instantiator instantiator) {
      super(ConfigurableCrossPlatformConfig.class, instantiator);
    }

}
