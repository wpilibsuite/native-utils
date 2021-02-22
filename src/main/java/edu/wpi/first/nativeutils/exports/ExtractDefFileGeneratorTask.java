package edu.wpi.first.nativeutils.exports;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.OutputFile;

public class ExtractDefFileGeneratorTask extends DefaultTask {
  private RegularFileProperty defFileGenerator;

  @OutputFile
  public RegularFileProperty getDefFileGenerator() {
    return defFileGenerator;
  }

  @Inject
  public ExtractDefFileGeneratorTask(ObjectFactory factory) {
    defFileGenerator = factory.fileProperty();
  }
}
