package edu.wpi.first.nativeutils.exports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import edu.wpi.first.toolchain.NativePlatforms;

public abstract class ExtractDefFileGeneratorTask extends DefaultTask {

  @OutputFile
  public abstract RegularFileProperty getDefFileGenerator();

  private ExecOperations operations;

    @Inject
    public ExtractDefFileGeneratorTask(ExecOperations operations) {
        this.operations = operations;
    }

  @TaskAction
  public void execute() {
    File file = getDefFileGenerator().getAsFile().get();
    String platformId = (NativePlatforms.desktopPlatformArch(operations) == NativePlatforms.x64arch ? "x64" : "arm64");
    InputStream is = ExportsConfigRules.class.getResourceAsStream("/" + platformId + "/DefFileGenerator.exe");
    OutputStream os = null;

    byte[] buffer = new byte[1024];
    int readBytes;
    try {
      os = new FileOutputStream(file);
      while ((readBytes = is.read(buffer)) != -1) {
        os.write(buffer, 0, readBytes);
      }
    } catch (IOException ex) {
    } finally {
      try {
        if (os != null) {
          os.close();
        }
        is.close();
      } catch (IOException ex) {
      }
    }
  }
}
