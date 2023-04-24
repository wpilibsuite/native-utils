package edu.wpi.first.nativeutils.exports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class PrivateExportsGenerationTask extends DefaultTask {

  @InputFile
  public abstract RegularFileProperty getSymbolsToExportFile();

  @OutputFile
  public abstract RegularFileProperty getExportsFile();

  @Internal
  public abstract ListProperty<String> getExportsList();

  @Input
  public abstract Property<String> getLibraryName();

  private boolean isWindows = false;
  private boolean isMac = false;

  public void setIsWindows(boolean set) {
    isWindows = set;
  }

  public void setIsMac(boolean set) {
    isMac = set;
  }

  private void executeWindows() throws IOException {
    List<String> exports = Files.readAllLines(getSymbolsToExportFile().get().getAsFile().toPath());
    getExportsList().addAll(exports);
    getExportsList().finalizeValue();

    File toWrite = getExportsFile().get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write("LIBRARY ");
      writer.write(getLibraryName().get());
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
    List<String> exports = Files.readAllLines(getSymbolsToExportFile().get().getAsFile().toPath());
    getExportsList().addAll(exports);
    getExportsList().finalizeValue();

    File toWrite = getExportsFile().get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write(getLibraryName().get());
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
    List<String> exports = Files.readAllLines(getSymbolsToExportFile().get().getAsFile().toPath());
    getExportsList().addAll(exports);
    getExportsList().finalizeValue();

    File toWrite = getExportsFile().get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
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
