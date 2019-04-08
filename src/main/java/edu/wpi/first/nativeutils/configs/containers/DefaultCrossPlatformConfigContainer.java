package edu.wpi.first.nativeutils.configs.containers;

import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

import edu.wpi.first.nativeutils.configs.CrossPlatformConfig;

public class DefaultCrossPlatformConfigContainer extends DefaultPolymorphicDomainObjectContainer<CrossPlatformConfig>
    implements CrossPlatformConfigContainer {

    public DefaultCrossPlatformConfigContainer(Instantiator instantiator) {
      super(CrossPlatformConfig.class, instantiator);
    }

}
