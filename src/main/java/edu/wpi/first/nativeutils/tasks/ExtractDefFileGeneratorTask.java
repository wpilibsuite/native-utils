package edu.wpi.first.nativeutils.tasks;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.OutputFile;

public class ExtractDefFileGeneratorTask extends DefaultTask {
  @OutputFile
  public RegularFileProperty defFileGenerator;

  @Inject
  public ExtractDefFileGeneratorTask(ObjectFactory factory) {
    defFileGenerator = factory.fileProperty();
  }
}
