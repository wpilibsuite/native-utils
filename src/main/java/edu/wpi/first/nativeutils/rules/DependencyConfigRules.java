package edu.wpi.first.nativeutils.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.internal.ProjectLayout;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.configs.CombinedDependencyConfig;
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

  private void staticLibraryPdbConfiguration(StaticLibraryBinarySpec staticLib, Project project,
      NativeUtilsExtension ext) {
    // Static libraries are special. Their pdb's are handled differently. To solve
    // this, we need to special case some pdbs
    // First, rename the output location
    if (!staticLib.getTargetPlatform().getOperatingSystem().isWindows()) {
      return;
    }

    Task lib = staticLib.getTasks().getCreateStaticLib();
    if (lib instanceof CreateStaticLibrary) {
      CreateStaticLibrary create = (CreateStaticLibrary) lib;
      File libFile = create.getOutputFile().get().getAsFile();
      File pdbRoot = libFile.getParentFile();
      String libPath = libFile.getAbsolutePath();
      libPath = libPath.substring(0, libPath.length() - 4);
      String outputLocation = libPath + ".pdb";

      String makePdbDirName = "makePdbDirFor" + staticLib.getBuildTask().getName();

      TaskProvider<Task> mkdirTask = project.getTasks().register(makePdbDirName, new Action<Task>() {
        @Override
        public void execute(Task task) {
          task.doLast(new Action<Task>() {
            @Override
            public void execute(Task arg0) {
              pdbRoot.mkdirs();
            }
          });
        }
      });

      String finalOutputLoc = outputLocation;

      staticLib.getTasks().withType(AbstractNativeSourceCompileTask.class).configureEach(it -> {
        it.dependsOn(mkdirTask);
        it.getOutputs().file(finalOutputLoc);
      });

      outputLocation = "/Fd" + outputLocation;

      staticLib.getCppCompiler().getArgs().add(outputLocation);
      staticLib.getcCompiler().getArgs().add(outputLocation);

      if (ext.getSourceLinkTask() != null) {
        String copySourceLinkName = "copySourceLink" + staticLib.getBuildTask().getName();

        TaskProvider<Copy> copyTask = project.getTasks().register(copySourceLinkName, Copy.class, new Action<Copy>() {
          @Override
          public void execute(Copy copy) {
            copy.from(ext.getSourceLinkTask().get().getSourceLinkBaseFile());
            copy.into(pdbRoot);
          }
        });
        create.dependsOn(copyTask);
      }
    }
  }

  @Mutate
  public void configureStaticPdbGeneration(ModelMap<Task> tasks, BinaryContainer binaries,
      ExtensionContainer ext, ProjectLayout projectLayout) {
    if (binaries == null) {
      return;
    }

    Project project = (Project) projectLayout.getProjectIdentifier();

    for (BinarySpec oBinary : binaries) {
      if (!(oBinary instanceof StaticLibraryBinarySpec)) {
        continue;
      }
      StaticLibraryBinarySpec binary = (StaticLibraryBinarySpec) oBinary;
      staticLibraryPdbConfiguration(binary, project, ext.getByType(NativeUtilsExtension.class));
    }
  }

  @Mutate
  public void setupDependencies(NativeDepsSpec libs, BinaryContainer binaries,
      final PlatformContainer platformContainer, ExtensionContainer extensions) {
    NativeUtilsExtension extension = extensions.getByType(NativeUtilsExtension.class);

    NamedDomainObjectCollection<DependencyConfig> dependencies = extension.getDependencyConfigs();
    NamedDomainObjectCollection<CombinedDependencyConfig> combinedDependencies = extension
        .getCombinedDependencyConfigs();

    ArrayList<String> allPlatforms = new ArrayList<>();

    for (Platform platform : platformContainer) {
      if (platform instanceof NativePlatform) {
        allPlatforms.add(platform.getName());
      }
    }

    String[] buildKinds = { "debug", "" };
    List<String> sharedMatchers = Arrays.asList("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
    List<String> runtimeMatchers = Arrays.asList("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll");
    List<String> sharedExcludes = Arrays.asList("**/*.so.debug", "**/*.so.*.debug");
    List<String> runtimeExcludes = Arrays.asList("**/*.so.debug", "**/*.so.*.debug");
    List<String> staticMatchers = Arrays.asList("**/*.lib", "**/*.a");

    for (DependencyConfig dependency : dependencies) {
      String name = dependency.getName();
      String config = "native_" + name;
      String mavenBase = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion()
          + ":";
      String mavenSuffix = "@" + dependency.getExt();

      boolean createdShared = false;
      boolean createdStatic = false;

      if (dependency.getHeaderClassifier() != null) {
        libs.create(name + "_headers", NativeLib.class, lib -> {
          setCommon(lib);
          lib.setTargetPlatforms(allPlatforms);
          lib.getHeaderDirs().add("");
          lib.setLibraryName(name + "_headers");
          lib.setMaven(mavenBase + dependency.getHeaderClassifier() + mavenSuffix);
          lib.setConfiguration(config + "_headers");
        });
      }

      if (dependency.getSourceClassifier() != null) {
        libs.create(name + "_sources", NativeLib.class, lib -> {
          setCommon(lib);
          lib.setTargetPlatforms(allPlatforms);
          lib.getSourceDirs().add("");
          lib.setLibraryName(name + "_sources");
          lib.setMaven(mavenBase + dependency.getSourceClassifier() + mavenSuffix);
          lib.setConfiguration(config + "_sources");
        });
      }

      for (String buildKind : buildKinds) {
        String buildType = buildKind.contains("debug") ? "debug" : "release";
        String binaryConfig = config + buildType + "_";

        for (String platform : dependency.getSharedPlatforms()) {
          createdShared = true;
          libs.create(name + "_shared_" + platform + buildType, NativeLib.class, lib -> {
            setCommon(lib);
            lib.setTargetPlatform(platform);
            lib.setLibraryName(name + "_shared_binaries");
            lib.setBuildType(buildType);
            lib.setSharedMatchers(new ArrayList<>(sharedMatchers));
            lib.setStaticMatchers(new ArrayList<>(staticMatchers));
            lib.setSharedExcludes(new ArrayList<>(sharedExcludes));
            lib.getSharedExcludes().addAll(dependency.getSharedExcludes());
            if (dependency.getSharedUsedAtRuntime()) {
              lib.setDynamicMatchers(new ArrayList<>(runtimeMatchers));
              lib.setDynamicExcludes(new ArrayList<>(runtimeExcludes));
            }
            lib.setMaven(mavenBase + platform + buildKind + mavenSuffix);
            lib.setConfiguration(binaryConfig + "shared_" + platform);
          });
        }

        for (String platform : dependency.getStaticPlatforms()) {
          createdStatic = true;
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

      if (createdShared) {
        libs.create(name + "_shared", CombinedNativeLib.class, lib -> {
          List<String> combinedLibs = lib.getLibs();
          combinedLibs.add(name + "_shared_binaries");
          if (dependency.getHeaderClassifier() != null) {
            combinedLibs.add(name + "_headers");
          }
          if (dependency.getSourceClassifier() != null) {
            combinedLibs.add(name + "_sources");
          }

          lib.getBuildTypes().add("debug");
          lib.getBuildTypes().add("release");
          lib.setTargetPlatforms(new ArrayList<>(dependency.getSharedPlatforms()));
        });
      }

      if (createdStatic) {
        libs.create(name + "_static", CombinedNativeLib.class, lib -> {
          List<String> combinedLibs = lib.getLibs();
          combinedLibs.add(name + "_static_binaries");
          if (dependency.getHeaderClassifier() != null) {
            combinedLibs.add(name + "_headers");
          }
          if (dependency.getSourceClassifier() != null) {
            combinedLibs.add(name + "_sources");
          }

          lib.getBuildTypes().add("debug");
          lib.getBuildTypes().add("release");
          lib.setTargetPlatforms(new ArrayList<>(dependency.getStaticPlatforms()));
        });
      }
    }

    for (CombinedDependencyConfig combined : combinedDependencies) {
      String name = combined.getName();
      String libraryName = combined.getLibraryName();
      List<String> deps = combined.getDependencies();
      List<String> targetPlatforms = combined.getTargetPlatforms();
      libs.create(name, CombinedNativeLib.class, lib -> {
        List<String> combinedLibs = lib.getLibs();
        combinedLibs.addAll(deps);

        lib.getBuildTypes().add("debug");
        lib.getBuildTypes().add("release");
        lib.setLibraryName(libraryName);
        lib.setTargetPlatforms(new ArrayList<>(targetPlatforms));
      });
    }
  }
}
