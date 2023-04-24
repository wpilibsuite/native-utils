package edu.wpi.first.nativeutils.vendordeps;

import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JavaArtifact;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JniArtifact;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.NamedJsonDependency;
import edu.wpi.first.toolchain.NativePlatforms;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

public class WPIJavaVendorDepsExtension {
  private final WPIVendorDepsExtension vendorDeps;
  private final ProviderFactory providerFactory;
  private final Project project;

  @Inject
  public WPIJavaVendorDepsExtension(WPIVendorDepsExtension vendorDeps, Project project) {
    this.vendorDeps = vendorDeps;
    this.providerFactory = project.getProviders();
    this.project = project;
  }

  public List<Provider<String>> java(String... ignore) {
    List<Provider<String>> deps = new ArrayList<>();

    for (NamedJsonDependency d : vendorDeps.getDependencySet()) {
      JsonDependency dep = d.getDependency();
      if (vendorDeps.isIgnored(ignore, dep)) {
        continue;
      }
      for (JavaArtifact art : dep.javaDependencies) {
        String baseId = art.groupId + ":" + art.artifactId;
        Callable<String> cbl = () -> baseId + ":" + vendorDeps.getVersion(art.version);

        try {
          project
              .getDependencies()
              .getComponents()
              .withModule(
                  baseId,
                  details -> {
                    details.allVariants(
                        varMeta -> {
                          varMeta.withDependencies(
                              col -> {
                                col.removeIf(item -> item.getGroup().startsWith("edu.wpi.first"));
                              });
                        });
                  });
        } catch (Exception ex) {
          Logger logger = Logging.getLogger(this.getClass());
          logger.warn(
              "Issue setting component metadata for "
                  + baseId
                  + ". Build could have issues with incorrect transitive dependencies.");
          logger.warn(
              "Please create an issue at https://github.com/wpilibsuite/allwpilib with this message so we can investigate");
        }

        deps.add(providerFactory.provider(cbl));
      }
    }
    return deps;
  }

  public List<Provider<String>> jniRelease(String platform, String... ignore) {
    return jniInternal(false, platform, ignore);
  }

  public List<Provider<String>> jniDebug(String platform, String... ignore) {
    return jniInternal(true, platform, ignore);
  }

  private List<Provider<String>> jniInternal(boolean debug, String platform, String... ignore) {
    boolean isRio = platform.equals(NativePlatforms.roborio);
    boolean hwSim = vendorDeps.isHwSimulation();
    List<Provider<String>> deps = new ArrayList<>();

    for (NamedJsonDependency d : vendorDeps.getDependencySet()) {
      JsonDependency dep = d.getDependency();
      if (!vendorDeps.isIgnored(ignore, dep)) {
        for (JniArtifact jni : dep.jniDependencies) {
          boolean applies =
              Arrays.asList(jni.validPlatforms).contains(platform)
                  && (isRio || (hwSim ? jni.useInHwSim() : jni.useInSwSim()));
          if (!applies && !jni.skipInvalidPlatforms)
            throw new MissingVendorJniDependencyException(dep.name, platform, jni);

          if (applies) {
            String debugString = debug ? "debug" : "";
            Callable<String> cbl =
                () ->
                    jni.groupId
                        + ":"
                        + jni.artifactId
                        + ":"
                        + vendorDeps.getVersion(jni.version)
                        + ":"
                        + platform
                        + debugString
                        + "@"
                        + (jni.isJar ? "jar" : "zip");
            deps.add(providerFactory.provider(cbl));
          }
        }
      }
    }
    return deps;
  }

  public static class MissingVendorJniDependencyException extends RuntimeException {
    private static final long serialVersionUID = -3526743142145446834L;
    private final String dependencyName;
    private final String classifier;
    private final WPIVendorDepsExtension.JniArtifact artifact;

    public String getDependencyName() {
      return dependencyName;
    }

    public String getClassifier() {
      return classifier;
    }

    public WPIVendorDepsExtension.JniArtifact getArtifact() {
      return artifact;
    }

    public MissingVendorJniDependencyException(
        String name, String classifier, JniArtifact artifact) {
      super("Cannot find jni dependency: " + name + " for classifier: " + classifier);
      this.dependencyName = name;
      this.classifier = classifier;
      this.artifact = artifact;
    }
  }
}
