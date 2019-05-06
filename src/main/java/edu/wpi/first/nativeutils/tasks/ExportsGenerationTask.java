package edu.wpi.first.nativeutils.tasks;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import groovy.transform.Internal;

public class ExportsGenerationTask extends DefaultTask {
  private final RegularFileProperty defFileGenerator;
  private final RegularFileProperty exportsFile;
  private final ListProperty<String> exportsList;
  private final Property<String> libraryName;

  @OutputFile
  public RegularFileProperty getExportsFile() {
    return exportsFile;
  }

  @Internal
  public ListProperty<String> getExportsList() {
    return exportsList;
  }

  @Input
  public RegularFileProperty getDefFileGenerator() {
    return defFileGenerator;
  }

  @Input
  public Property<String> getLibraryName() {
    return libraryName;
  }

  @Internal
  public void setLinkTask(AbstractLinkTask linkTask) {
    this.getInputs().files(linkTask.getSource());
    this.getInputs().files(linkTask.getLibs());
  }

  @Inject
  public ExportsGenerationTask(ObjectFactory factory) {
    exportsFile = factory.fileProperty();
    exportsList = factory.listProperty(String.class);
    libraryName = factory.property(String.class);
    defFileGenerator = factory.fileProperty();
  }

  @TaskAction
  public void execute() {
    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    Project project = getProject();

    String exeName = defFileGenerator.getAsFile().get().toString();
    File defFile = exportsFile.getAsFile().get();

    project.exec(exec -> {
      exec.setExecutable(exeName);
      exec.args(defFile);
      exec.args(((AbstractLinkTask) sBinary.getTasks().getLink()).getSource());
  });
  }
}
