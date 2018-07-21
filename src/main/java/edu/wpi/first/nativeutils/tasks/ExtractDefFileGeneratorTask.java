package edu.wpi.first.nativeutils.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

public class ExtractDefFileGeneratorTask extends DefaultTask implements Task {
  @OutputFile
  public RegularFileProperty defFileGenerator = newOutputFile();
}
