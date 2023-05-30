package edu.wpi.first.nativeutils.java;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class NativeMethodsGenerationTask extends DefaultTask {
  private final RegularFileProperty inputFile;
  private final RegularFileProperty outputFile;

  @InputFile
  public RegularFileProperty getInputFile() {
    return inputFile;
  }

  @OutputFile
  public RegularFileProperty getOutputFile() {
    return outputFile;
  }

  @Inject
  public NativeMethodsGenerationTask() {
    ObjectFactory objects = getProject().getObjects();
    inputFile = objects.fileProperty();
    outputFile = objects.fileProperty();
  }

  @TaskAction
  public void execute() throws IOException {
    NativeMethodsGenerator generator = new NativeMethodsGenerator();
    List<String> results = generator.generate(inputFile.get().getAsFile().toPath());
    Files.write(outputFile.get().getAsFile().toPath(), results, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }
}
