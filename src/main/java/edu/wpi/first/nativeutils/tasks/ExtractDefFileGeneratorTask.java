package edu.wpi.first.nativeutils.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class ExtractDefFileGeneratorTask extends DefaultTask {
  @OutputFile
  public RegularFileProperty defFileGenerator;

  @Inject
  public ExtractDefFileGeneratorTask(Project rootProject, ObjectFactory factory) {
    defFileGenerator = factory.fileProperty();
    defFileGenerator.set(rootProject.getLayout().getBuildDirectory().file("DefFileGenerator.exe"));
  }

  @TaskAction
  public void execute() {
    File file = defFileGenerator.getAsFile().get();
    InputStream is = ExtractDefFileGeneratorTask.class.getResourceAsStream("/DefFileGenerator.exe");
    OutputStream os = null;

    byte[] buffer = new byte[0xFFFF];
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
