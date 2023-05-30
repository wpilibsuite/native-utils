package edu.wpi.first.nativeutils.java;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class PreloadListGenerationTask extends DefaultTask {
  private final ListProperty<File> classPath;
  private final RegularFileProperty preloadListFile;
  private final ListProperty<String> excludePrefixes;
  private final ListProperty<String> includePrefixes;
  private final ListProperty<String> excludeClasses;
  private final ListProperty<String> includeClasses;

  @InputFiles
  public ListProperty<File> getClassPath() {
    return classPath;
  }

  @OutputFile
  public RegularFileProperty getPreloadListFile() {
    return preloadListFile;
  }

  @Input
  public ListProperty<String> getExcludePrefixes() {
    return excludePrefixes;
  }

  @Input
  public ListProperty<String> getIncludePrefixes() {
    return includePrefixes;
  }

  @Input
  public ListProperty<String> getExcludeClasses() {
    return excludeClasses;
  }

  @Input
  public ListProperty<String> getIncludeClasses() {
    return includeClasses;
  }

  @Inject
  public PreloadListGenerationTask() {
    ObjectFactory objects = getProject().getObjects();
    classPath = objects.listProperty(File.class);
    preloadListFile = objects.fileProperty();
    excludePrefixes = objects.listProperty(String.class);
    includePrefixes = objects.listProperty(String.class);
    excludeClasses = objects.listProperty(String.class);
    includeClasses = objects.listProperty(String.class);
  }

  @TaskAction
  public void execute() throws IOException {
    try (PreloadScanner scanner = new PreloadScanner()) {
      scanner.setClassPath(classPath.get());
      for (String prefix : excludePrefixes.get()) {
        scanner.addIgnoredPrefix(prefix);
      }
      for (String prefix : includePrefixes.get()) {
        scanner.removeIgnoredPrefix(prefix);
      }
      for (String className : excludeClasses.get()) {
        scanner.ignoreClass(className);
      }
      for (String className : includeClasses.get()) {
        scanner.scan(className);
      }
      Files.write(preloadListFile.get().getAsFile().toPath(), scanner.getResults(), Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
  }
}
