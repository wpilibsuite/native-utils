package edu.wpi.first.nativeutils.configs.containers;

import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;

import edu.wpi.first.nativeutils.configs.NativePlatformConfig;

public class DefaultNativePlatformConfigContainer extends DefaultPolymorphicDomainObjectContainer<NativePlatformConfig>
    implements NativePlatformConfigContainer {

    public DefaultNativePlatformConfigContainer(Instantiator instantiator) {
      super(NativePlatformConfig.class, instantiator);
    }

}
