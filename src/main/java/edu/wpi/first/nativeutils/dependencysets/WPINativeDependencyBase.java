package edu.wpi.first.nativeutils.dependencysets;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeDependencySet;

public abstract class WPINativeDependencyBase implements NativeDependencySet {
  protected final Project m_project;
  private FileCollection m_headers;
  private final Map<String, File> extractedMap = new HashMap<>();

  private String m_headerConfigName;

  protected final FileCollection m_emptyCollection;

  public WPINativeDependencyBase(Project project, String headerConfigName) {
    this.m_project = project;
    this.m_headerConfigName = headerConfigName;
    m_emptyCollection = project.files();
  }

  protected File extractDependency(String name, String configurationName) {
    File fileBase = extractedMap.get(name);
    if (fileBase != null) {
      return fileBase;
    }

    Configuration config = m_project.getConfigurations().getByName(configurationName);
    DependencySet dependencies = config.getDependencies();

    Dependency first = dependencies.iterator().next();
    File file = config.files(first).iterator().next();

    String dirBase = Paths.get(m_project.getBuildDir().toString(), "extractedDependencies", name).toString();

    m_project.delete(dirBase);

    m_project.copy(copy -> {

      copy.from(m_project.zipTree(file));
      copy.into(dirBase);
    });

    File toRet = m_project.file(dirBase);
    extractedMap.put(name, toRet);
    return toRet;
  }

  private void resolveHeaderConfigs() {
    if (m_headers != null) {
      return;
    }
    File headerRoot = extractDependency(m_headerConfigName, m_headerConfigName);
    m_headers = m_project.files(headerRoot);
}

  @Override
  public FileCollection getIncludeRoots() {
      resolveHeaderConfigs();
      return m_headers;
  }
}
