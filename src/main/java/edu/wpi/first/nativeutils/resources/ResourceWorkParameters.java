package edu.wpi.first.nativeutils.resources;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface ResourceWorkParameters extends WorkParameters {
    RegularFileProperty getSourceFile();
    RegularFileProperty getOutputFile();
    Property<String> getNamespace();
    Property<String> getFuncName();
}
