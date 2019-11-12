package edu.wpi.first.nativeutils.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class PrivateExportsGenerationTask extends DefaultTask {
  private final RegularFileProperty symbolsToExportFile;

  private final RegularFileProperty exportsFile;

  private final ListProperty<String> exportsList;

  private final Property<String> libraryName;

  @InputFile
  public RegularFileProperty getSymbolsToExportFile() {
    return symbolsToExportFile;
  }

  @OutputFile
  public RegularFileProperty getExportsFile() {
    return exportsFile;
  }

  @Internal
  public ListProperty<String> getExportsList() {
    return exportsList;
  }

  @Input
  public Property<String> getLibraryName() {
    return libraryName;
  }

  private boolean isWindows = false;
  private boolean isMac = false;

  public void setIsWindows(boolean set) {
    isWindows = set;
  }

  public void setIsMac(boolean set) {
    isMac = set;
  }

  @Inject
  public PrivateExportsGenerationTask(ObjectFactory factory) {
    symbolsToExportFile = factory.fileProperty();
    exportsFile = factory.fileProperty();
    exportsList = factory.listProperty(String.class);
    libraryName = factory.property(String.class);

    this.getInputs().file(symbolsToExportFile);
    this.getOutputs().file(exportsFile);
  }

  private void executeWindows() throws IOException {
    List<String> exports = Files.readAllLines(symbolsToExportFile.get().getAsFile().toPath());
    exportsList.addAll(exports);
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write("LIBRARY ");
      writer.write(libraryName.get());
      writer.newLine();
      writer.write("EXPORTS");
      writer.newLine();
      for (String export : exports) {
        writer.write("  ");
        writer.write(export);
        writer.newLine();
      }
      writer.flush();
    }
  }

  private void executeUnix() throws IOException {
    List<String> exports = Files.readAllLines(symbolsToExportFile.get().getAsFile().toPath());
    exportsList.addAll(exports);
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write(libraryName.get());
      writer.write(" {");
      writer.newLine();
      writer.write("  global: ");
      for (String export : exports) {
        writer.write(export);
        writer.write("; ");
      }
      writer.newLine();
      writer.write("  local: *;");
      writer.newLine();
      writer.write("};");
      writer.newLine();
      writer.flush();
    }
  }

  private void executeMac() throws IOException {
    List<String> exports = Files.readAllLines(symbolsToExportFile.get().getAsFile().toPath());
    exportsList.addAll(exports);
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
      for (String export : exports) {
        writer.write("_");
        writer.write(export);
        writer.newLine();
      }
      writer.flush();
    }
  }

  @TaskAction
  public void execute() throws IOException {
    if (isWindows) {
      executeWindows();
    } else if (isMac) {
      executeMac();
    } else {
      executeUnix();
    }
  }
}
