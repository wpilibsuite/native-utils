package edu.wpi.first.nativeutils.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

public class ExtractDefFileGeneratorTask extends DefaultTask {
  @OutputFile
  public RegularFileProperty defFileGenerator = newOutputFile();
}
