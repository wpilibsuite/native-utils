package edu.wpi.first.toolchain;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class ToolchainExtensionBase {
  private Property<Boolean> m_optional;

  @Inject
  public ObjectFactory getObjectFactory() {
      // Method body is ignored
      throw new UnsupportedOperationException();
  }

  public Property<Boolean> IsOptional() {
    if (m_optional == null) {
      m_optional = getObjectFactory().property(Boolean.class);
      m_optional.set(true);
    }
    return m_optional;
  }
}
