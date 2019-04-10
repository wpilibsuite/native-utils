package edu.wpi.first.nativeutils.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.DependencyConfig;
import jaci.gradle.nativedeps.CombinedNativeLib;
import jaci.gradle.nativedeps.NativeDepsSpec;
import jaci.gradle.nativedeps.NativeLib;

public class DependencyConfigRules extends RuleSource {
  private void setCommon(NativeLib lib) {
    lib.setHeaderDirs(new ArrayList<>());
    lib.setSourceDirs(new ArrayList<>());
    lib.setStaticMatchers(new ArrayList<>());
    ArrayList<String> debugMatchers = new ArrayList<>();
    debugMatchers.add("**/*.pdb");
    debugMatchers.add("**/*.so.debug");
    debugMatchers.add("**/*.so.*.debug");
    lib.setDebugMatchers(debugMatchers);
    lib.setSharedMatchers(new ArrayList<>());
    lib.setDynamicMatchers(new ArrayList<>());
  }

  @Mutate
  public void setupDependencies(NativeDepsSpec libs, BinaryContainer binaries, final PlatformContainer platformContainer, ExtensionContainer extensions) {
    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    NamedDomainObjectCollection<DependencyConfig> dependencies = extension.getDependencyConfigs();

    ArrayList<String> allPlatforms = new ArrayList<>();

    for (Platform platform : platformContainer) {
      if (platform instanceof NativePlatform) {
        allPlatforms.add(platform.getName());
      }
    }

    String[] buildKinds = {"debug", ""};
    List<String> sharedMatchers = Arrays.asList("**/*.so", "**/*.so.*", "**/*.dll");
    List<String> sharedExcludes = Arrays.asList("**/*.so.debug", "**/*.so.*.debug");
    List<String> staticMatchers = Arrays.asList("**/*.lib", "**/*.a");

    for (DependencyConfig dependency : dependencies) {
      String name = dependency.getName();
      String config = "native_" + name;
      String mavenBase = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() + ":";
      String mavenSuffix = "@" + dependency.getExt();

      libs.create(name + "_headers", NativeLib.class, lib -> {
        setCommon(lib);
        lib.setTargetPlatforms(allPlatforms);
        lib.getHeaderDirs().add("");
        lib.setLibraryName(name + "_headers");
        lib.setMaven(mavenBase + dependency.getHeaderClassifier() + mavenSuffix);
        lib.setConfiguration(config);
      });

      if (dependency.getSourceClassifier() != null) {
        libs.create(name + "_sources", NativeLib.class, lib -> {
          setCommon(lib);
          lib.setTargetPlatforms(allPlatforms);
          lib.getHeaderDirs().add("");
          lib.setLibraryName(name + "_sources");
          lib.setMaven(mavenBase + dependency.getSourceClassifier() + mavenSuffix);
          lib.setConfiguration(config);
        });
      }

      for (String buildKind : buildKinds) {
        String buildType = buildKind.contains("debug") ? "debug" : "release";
        String binaryConfig = config + buildType + "_";

        for (String platform : dependency.getSharedPlatforms()) {
          libs.create(name + "_shared_" + platform + buildType, NativeLib.class, lib -> {
            setCommon(lib);
            lib.setTargetPlatform(platform);
            lib.setLibraryName(name + "_shared_binaries");
            lib.setBuildType(buildType);
            lib.setSharedMatchers(new ArrayList<>(sharedMatchers));
            lib.setStaticMatchers(new ArrayList<>(staticMatchers));
            lib.setSharedExcludes(new ArrayList<>(sharedExcludes));
            if (dependency.getSharedUsedAtRuntime()) {
              lib.setDynamicMatchers(new ArrayList<>(sharedMatchers));
            }
            lib.setMaven(mavenBase + platform + buildKind + mavenSuffix);
            lib.setConfiguration(binaryConfig + "shared_" + platform);
          });
        }

        for (String platform : dependency.getStaticPlatforms()) {
          libs.create(name + "_static_" + platform + buildType, NativeLib.class, lib -> {
            setCommon(lib);
            lib.setTargetPlatform(platform);
            lib.setLibraryName(name + "_static_binaries");
            lib.setBuildType(buildType);
            lib.setSharedMatchers(new ArrayList<>(sharedMatchers));
            lib.setStaticMatchers(new ArrayList<>(staticMatchers));
            lib.setSharedExcludes(new ArrayList<>(sharedExcludes));
            lib.setMaven(mavenBase + platform + "static" + buildKind + mavenSuffix);
            lib.setConfiguration(binaryConfig + "static_" + platform);
          });
        }
      }


      libs.create(name + "_shared", CombinedNativeLib.class, lib -> {
        List<String> combinedLibs = lib.getLibs();
        combinedLibs.add(name + "_shared_binaries");
        combinedLibs.add(name + "_headers");
        if (dependency.getSourceClassifier() != null) {
          combinedLibs.add(name + "_sources");
        }

        lib.getBuildTypes().add("debug");
        lib.getBuildTypes().add("release");
        lib.setTargetPlatforms(new ArrayList<>(dependency.getSharedPlatforms()));
      });

      libs.create(name + "_static", CombinedNativeLib.class, lib -> {
        List<String> combinedLibs = lib.getLibs();
        combinedLibs.add(name + "_static_binaries");
        combinedLibs.add(name + "_headers");
        if (dependency.getSourceClassifier() != null) {
          combinedLibs.add(name + "_sources");
        }

        lib.getBuildTypes().add("debug");
        lib.getBuildTypes().add("release");
        lib.setTargetPlatforms(new ArrayList<>(dependency.getStaticPlatforms()));
      });
    }
  }
}
