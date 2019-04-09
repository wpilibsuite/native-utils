package edu.wpi.first.nativeutils.configs.containers;

import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class DefaultPlatformConfigContainer extends DefaultPolymorphicDomainObjectContainer<PlatformConfig>
    implements PlatformConfigContainer {

    public DefaultPlatformConfigContainer(Instantiator instantiator) {
      super(PlatformConfig.class, instantiator);
    }

}
