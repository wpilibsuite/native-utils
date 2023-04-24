package edu.wpi.first.nativeutils.dependencies;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.MapProperty;
import org.gradle.nativeplatform.NativeBinarySpec;

public abstract class CombinedNativeDependency implements NativeDependency {

  private final String name;
  private final NamedDomainObjectCollection<NativeDependency> dependencyCollection;

  @Inject
  public CombinedNativeDependency(
      String name, NamedDomainObjectCollection<NativeDependency> dependencyCollection) {
    this.name = name;
    this.dependencyCollection = dependencyCollection;
  }

  public abstract MapProperty<String, List<String>> getDependencies();

  @Inject
  public ProjectLayout getProjectLayout() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ResolvedNativeDependency resolveNativeDependency(
      NativeBinarySpec binary, FastDownloadDependencySet loaderDependencySet) {
    Map<String, List<String>> dependencies = getDependencies().get();

    List<String> depsForPlatform =
        dependencies.getOrDefault(binary.getTargetPlatform().getName(), null);
    if (depsForPlatform == null) {
      return null;
    }

    ProjectLayout projectLayout = getProjectLayout();

    FileCollection includeRoots = projectLayout.files();
    FileCollection sourceRoots = projectLayout.files();
    FileCollection linkFiles = projectLayout.files();
    FileCollection runtimeFiles = projectLayout.files();

    for (String dep : depsForPlatform) {
      ResolvedNativeDependency resolved =
          dependencyCollection.getByName(dep).resolveNativeDependency(binary, loaderDependencySet);
      includeRoots = includeRoots.plus(resolved.getIncludeRoots());
      sourceRoots = sourceRoots.plus(resolved.getSourceRoots());
      linkFiles = linkFiles.plus(resolved.getLinkFiles());
      runtimeFiles = runtimeFiles.plus(resolved.getRuntimeFiles());
    }

    return new ResolvedNativeDependency(includeRoots, sourceRoots, linkFiles, runtimeFiles);
  }
}
