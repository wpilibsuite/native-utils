package edu.wpi.first.nativeutils;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.nativeplatform.TargetedNativeComponent;

public class UsePlatformHandler implements Action<Object[]> {

  @Override
  public void execute(Object[] arguments) {
    if (arguments.length < 2) {
      throw new GradleException("Not enough arguments passed, need at least 2");
    }
    if (!(arguments[0] instanceof TargetedNativeComponent)) {
      throw new GradleException("usePlatform must be placed directly in the component");
    }
    TargetedNativeComponent component = (TargetedNativeComponent)arguments[0];
    for (int i = 1; i < arguments.length; i++) {
      component.targetPlatform((String)arguments[i]);
    }
  }

}
