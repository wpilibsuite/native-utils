package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.platform.base.VariantComponentSpec;

//import edu.wpi.first.deployutils.files.DefaultDirectoryTree;
//import edu.wpi.first.deployutils.files.IDirectoryTree;
//import edu.wpi.first.deployutils.nativedeps.DelegatedDependencySet;
//import edu.wpi.first.deployutils.nativedeps.DependencySpecExtension;
// import edu.wpi.first.nativeutils.configs.CombinedDependencyConfig;
// import edu.wpi.first.nativeutils.configs.DependencyConfig;
// import edu.wpi.first.nativeutils.configs.impl.DefaultCombinedDependencyConfig;
// import edu.wpi.first.nativeutils.configs.impl.DefaultDependencyConfig;
import edu.wpi.first.nativeutils.dependencies.DelegatedDependencySet;
import edu.wpi.first.nativeutils.dependencies.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.CombinedIgnoreMissingPlatformNativeDependency;
import edu.wpi.first.nativeutils.dependencies.CombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.WPISharedMavenDependency;
import edu.wpi.first.nativeutils.dependencies.WPIStaticMavenDependency;
import edu.wpi.first.nativeutils.exports.DefaultExportsConfig;
import edu.wpi.first.nativeutils.exports.ExportsConfig;
import edu.wpi.first.nativeutils.exports.PrivateExportsConfig;
import edu.wpi.first.nativeutils.platforms.DefaultPlatformConfig;
import edu.wpi.first.nativeutils.platforms.PlatformConfig;
// import edu.wpi.first.nativeutils.configs.impl.DefaultPrivateExportsConfig;
// import edu.wpi.first.nativeutils.rules.FrcNativeBinaryExtension;
import edu.wpi.first.nativeutils.sourcelink.SourceLinkPlugin;
import edu.wpi.first.nativeutils.tasks.PrintNativeDependenciesTask;
import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptorBase;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.bionic.BionicToolchainPlugin;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.raspbian.RaspbianToolchainPlugin;
import edu.wpi.first.toolchain.roborio.RoboRioToolchainPlugin;

public class NativeUtilsExtension {

  private final NamedDomainObjectContainer<PlatformConfig> platformConfigs;

  private final NamedDomainObjectContainer<ExportsConfig> exportsConfigs;

  private final ExtensiblePolymorphicDomainObjectContainer<NativeDependency> dependencyContainer;

  // private final NamedDomainObjectContainer<DelegatedDependencySet> delegatedDependencyContainer;
  // private final NamedDomainObjectContainer<DelegatedDependencySet> optionalDelegatedDependencyContainer;

  // private final NamedDomainObjectContainer<DependencyConfig> dependencyConfigs;

  // private final NamedDomainObjectContainer<CombinedDependencyConfig> combinedDependencyConfigs;

  private final NamedDomainObjectContainer<PrivateExportsConfig> privateExportsConfigs;

  // private final NamedDomainObjectContainer<NativeLibraryConfig> nativeLibraryConfigs;

  // private final NamedDomainObjectContainer<CombinedNativeLibraryConfig> combinedNativeLibraryConfigs;

  private final TaskProvider<PrintNativeDependenciesTask> printNativeDependenciesTask;

  // public static class NamedNativeDependencyList implements Named {
  //   private final String name;
  //   private final List<BaseLibraryDependencySet> deps = new ArrayList<>();
  //   private boolean skipMissingPlatform;

  //   @Override
  //   public String getName() {
  //     return name;
  //   }

  //   public List<BaseLibraryDependencySet> getDeps() {
  //     return deps;
  //   }

  //   public boolean isSkipMissingPlatform() {
  //     return skipMissingPlatform;
  //   }

  //   public void setSkipMissingPlatform(boolean skipMissingPlatform) {
  //     this.skipMissingPlatform = skipMissingPlatform;
  //   }

  //   @Inject
  //   public NamedNativeDependencyList(String name) {
  //     this.name = name;
  //   }

  //   public void printDependencies() {
  //     for (BaseLibraryDependencySet set : deps) {
  //       System.out.println("Dep " + set.getName());
  //       var buildTypes = set.getBuildTypes();
  //       if (buildTypes == null) {
  //         System.out.println("  Build Type is Null");
  //       } else {
  //         System.out.print("    BuildTypes: [");
  //         for (String bt : buildTypes) {
  //           System.out.print(bt + ", ");
  //         }
  //         System.out.println("]");
  //       }

  //       var flavors = set.getFlavors();
  //       if (flavors == null) {
  //         System.out.println("  Flavors is Null");
  //       } else {
  //         System.out.print("    Flavors: [");
  //         for (String bt : flavors) {
  //           System.out.print(bt + ", ");
  //         }
  //         System.out.println("]");
  //       }

  //       var platforms = set.getTargetPlatforms();
  //       if (platforms == null) {
  //         System.out.println("  Platforms is Null");
  //       } else {
  //         System.out.print("    Platforms: [");
  //         for (String bt : platforms) {
  //           System.out.print(bt + ", ");
  //         }
  //         System.out.println("]");
  //       }
  //     }
  //   }

  //   public BaseLibraryDependencySet findAppliesTo(NativeBinarySpec binary) {
  //     BaseLibraryDependencySet found = null;
  //     for (BaseLibraryDependencySet set : deps) {
  //       if (set.appliesTo(binary.getFlavor().getName(), binary.getBuildType().getName(), binary.getTargetPlatform().getName())) {
  //         if (found != null) {
  //           throw new GradleException("Multiple possble dependencies found for " + binary.getName());
  //         }
  //         found = set;
  //       }
  //     }
  //     return found;
  //   }

  // }

  public TaskProvider<PrintNativeDependenciesTask> getPrintNativeDependenciesTask() {
    return printNativeDependenciesTask;
  }

  // public NamedDomainObjectContainer<DelegatedDependencySet> getDelegatedDependencyContainer() {
  //   return delegatedDependencyContainer;
  // }

  // private final NamedDomainObjectContainer<NamedNativeDependencyList> nativeLibraryDependencySets;

  // private final NamedDomainObjectContainer<FrcNativeBinaryExtension> nativeBinaryExt;

  private final Project project;

  private final List<String> platformsToConfigure = new ArrayList<>();

  private final ObjectFactory objectFactory;

  private final ToolchainExtension tcExt;

  public Class<? extends NativeDependency> getNativeDependencyTypeClass(String name) {
    @SuppressWarnings("unchecked")
    PolymorphicDomainObjectContainerInternal<NativeDependency> internalDependencies =
        (PolymorphicDomainObjectContainerInternal<NativeDependency>) dependencyContainer;
    Set<? extends java.lang.Class<? extends NativeDependency>> dependencyTypeSet = internalDependencies.getCreateableTypes();
    for (Class<? extends NativeDependency> dependencyType : dependencyTypeSet) {
        if (dependencyType.getSimpleName().equals(name)) {
            return dependencyType;
        }
    }
    return null;
}

  private <T extends NativeDependency> void addNativeDependencyType(Class<T> cls, Object arg) {
    dependencyContainer.registerFactory(cls, name -> {
      return objectFactory.newInstance(cls, name, arg);
    });
  }

  @Inject
  public NativeUtilsExtension(Project project, ToolchainExtension tcExt) {
    this.project = project;
    this.tcExt = tcExt;
    this.objectFactory = project.getObjects();

    exportsConfigs = objectFactory.domainObjectContainer(ExportsConfig.class, name -> {
      return objectFactory.newInstance(DefaultExportsConfig.class, name);
    });

    dependencyContainer = objectFactory.polymorphicDomainObjectContainer(NativeDependency.class);
    addNativeDependencyType(WPIStaticMavenDependency.class, project);
    addNativeDependencyType(WPISharedMavenDependency.class, project);

    addNativeDependencyType(CombinedIgnoreMissingPlatformNativeDependency.class, dependencyContainer);
    addNativeDependencyType(AllPlatformsCombinedNativeDependency.class, dependencyContainer);
    addNativeDependencyType(CombinedNativeDependency.class, dependencyContainer);

    // delegatedDependencyContainer = objectFactory.domainObjectContainer(DelegatedDependencySet.class, name -> {
    //   return objectFactory.newInstance(DelegatedDependencySet.class, name, dependencyContainer, true);
    // });

    // optionalDelegatedDependencyContainer = objectFactory.domainObjectContainer(DelegatedDependencySet.class, name -> {
    //   return objectFactory.newInstance(DelegatedDependencySet.class, name, dependencyContainer, false);
    // });



    // dependencyConfigs = new AfterAddNamedDomainObjectContainer<>(DependencyConfig.class, name -> {
    //   return objectFactory.newInstance(DefaultDependencyConfig.class, name);
    // });

    platformConfigs = objectFactory.domainObjectContainer(PlatformConfig.class, name -> {
      return (PlatformConfig)objectFactory.newInstance(DefaultPlatformConfig.class, name);
    });

    // combinedDependencyConfigs = new AfterAddNamedDomainObjectContainer<>(CombinedDependencyConfig.class, name -> {
    //   return objectFactory.newInstance(DefaultCombinedDependencyConfig.class, name);
    // });

    privateExportsConfigs = objectFactory.domainObjectContainer(PrivateExportsConfig.class, name -> {
      return objectFactory.newInstance(PrivateExportsConfig.class, name);
    });

    // nativeLibraryConfigs = new AfterAddNamedDomainObjectContainer<>(NativeLibraryConfig.class, name -> {
    //   return objectFactory.newInstance(DefaultNativeLibraryConfig.class, name);
    // });

    // combinedNativeLibraryConfigs = new AfterAddNamedDomainObjectContainer<>(CombinedNativeLibraryConfig.class, name -> {
    //   DefaultCombinedNativeLibraryConfig newInst = objectFactory.newInstance(DefaultCombinedNativeLibraryConfig.class,
    //       name);
    //   newInst.setLibs(new ArrayList<>());
    //   return newInst;
    // });

    // nativeLibraryDependencySets = new AfterAddNamedDomainObjectContainer<>(NamedNativeDependencyList.class, name -> {
    //   return objectFactory.newInstance(NamedNativeDependencyList.class, name);
    // });

    // nativeBinaryExt = new AfterAddNamedDomainObjectContainer<>(FrcNativeBinaryExtension.class, name -> {
    //   return objectFactory.newInstance(FrcNativeBinaryExtension.class, name);
    // });

    project.afterEvaluate(proj -> {
      for (PlatformConfig config : platformConfigs) {
        if (config.getPlatformPath() == null) {
          throw new GradleException("Platform Path cannot be null: " + config.getName());
        }
      }
    });

    printNativeDependenciesTask = project.getTasks().register("printNativeDependencyGraph", PrintNativeDependenciesTask.class);

    // project.getDependencies().registerTransform(UnzipTransform.class,
    //     new Action<TransformSpec<TransformParameters.None>>() {
    //       @Override
    //       public void execute(TransformSpec<TransformParameters.None> variantTransform) {
    //         variantTransform.getFrom().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ZIP_TYPE);
    //         variantTransform.getTo().attribute(ArtifactAttributes.ARTIFACT_FORMAT, DIRECTORY_TYPE);
    //       }
    //     });

    // combinedDependencyConfigs.all(this::handleNewCombinedDependency);
    // nativeLibraryConfigs.all(this::handleNewNativeLibrary);
    // combinedNativeLibraryConfigs.all(this::handleNewCombinedNativeLibrary);
    // dependencyConfigs.all(this::handleNewDependency);
  }

  public ExtensiblePolymorphicDomainObjectContainer<NativeDependency> getNativeDependencyContainer() {
    return dependencyContainer;
  }

  // public NamedDomainObjectSet<NamedNativeDependencyList> getNativeLibraryDependencySets() {
  //   return nativeLibraryDependencySets;
  // }

//   private void handleNewCombinedNativeLibrary(CombinedNativeLibraryConfig lib) {
//     String uniqName = lib.getName();
//     String libName = lib.getLibraryName() == null ? uniqName : lib.getLibraryName();

//     CombinedLibraryDependencySet dep = new CombinedLibraryDependencySet(uniqName, lib.getLibs(), getPlatforms(lib), getFlavors(lib), getBuildTypes(lib));
//     NamedNativeDependencyList nativeDep = nativeLibraryDependencySets.maybeCreate(libName);
//     if (!lib.isSkipMissingPlatform() && nativeDep.isSkipMissingPlatform()) {
//       throw new GradleException("Unusable configuration of dependency skipping");
//     }
//     nativeDep.setSkipMissingPlatform(lib.isSkipMissingPlatform());
//     nativeDep.getDeps().add(dep);
//   }

//   private void handleNewNativeLibrary(NativeLibraryConfig lib) {
//     String uniqName = lib.getName();
//     String libName = lib.getLibraryName() == null ? uniqName : lib.getLibraryName();

//     Supplier<FileTree> rootTree = addDependency(project, lib);

//     FileCollection sharedFiles = matcher(project, rootTree, lib.getSharedMatchers(), lib.getSharedExcludes());
//     FileCollection staticFiles = matcher(project, rootTree, lib.getStaticMatchers(), lib.getStaticExcludes());
//     FileCollection debugFiles = matcher(project, rootTree, lib.getDebugMatchers(), lib.getDebugExcludes());
//     FileCollection dynamicFiles = matcher(project, rootTree, lib.getDynamicMatchers(), lib.getDynamicExcludes());

//     IDirectoryTree headerFiles = new DefaultDirectoryTree(rootTree,
//         lib.getHeaderDirs() == null ? new ArrayList<>() : lib.getHeaderDirs());
//     IDirectoryTree sourceFiles = new DefaultDirectoryTree(rootTree,
//         lib.getSourceDirs() == null ? new ArrayList<>() : lib.getSourceDirs());

//     NativeLibraryDependencySet depSet = new NativeLibraryDependencySet(
//         project, uniqName,
//         headerFiles, sourceFiles, staticFiles.plus(sharedFiles),
//         dynamicFiles, debugFiles, lib.getSystemLibs() == null ? new ArrayList<>() : lib.getSystemLibs(),
//         getPlatforms(lib), getFlavors(lib), getBuildTypes(lib)
//     );

//     NamedNativeDependencyList nativeDep = nativeLibraryDependencySets.maybeCreate(libName);
//     if (!lib.isSkipMissingPlatform() && nativeDep.isSkipMissingPlatform()) {
//       throw new GradleException("Unusable configuration of dependency skipping single dep");
//     }
//     nativeDep.setSkipMissingPlatform(lib.isSkipMissingPlatform());
//     nativeDep.getDeps().add(depSet);
//   }

//   private static Set<String> getFlavors(BaseNativeLibraryConfig lib) {
//     if (lib.getFlavor() == null && (lib.getFlavors() == null || lib.getFlavors().isEmpty()))
//         return Set.of();
//     Set<String> fl = lib.getFlavors() == null ? Set.of(lib.getFlavor()) : lib.getFlavors();
//     return fl;
// }

// private static Set<String> getBuildTypes(BaseNativeLibraryConfig lib) {
//     if (lib.getBuildType() == null && (lib.getBuildTypes() == null || lib.getBuildTypes().isEmpty()))
//         return Set.of();
//     Set<String> fl = lib.getBuildTypes() == null ? Set.of(lib.getBuildType()) : lib.getBuildTypes();
//     return fl;
// }

// private static Set<String> getPlatforms(BaseNativeLibraryConfig lib) {
//     if (lib.getTargetPlatform() == null && (lib.getTargetPlatforms() == null || lib.getTargetPlatforms().isEmpty()))
//         return Set.of();
//     Set<String> fl = lib.getTargetPlatforms() == null ? Set.of(lib.getTargetPlatform()) : lib.getTargetPlatforms();
//     return fl;
// }

//   private static FileCollection matcher(Project proj, Supplier<FileTree> tree, List<String> matchers,
//       List<String> excludes) {
//     return proj.files(new Callable<FileCollection>() {

//       @Override
//       public FileCollection call() throws Exception {
//         return tree.get().matching(new Action<PatternFilterable>() {

//           @Override
//           public void execute(PatternFilterable filter) {
//             // <<!!ET_NOMATCH!!> is a magic string in the case the matchers are null.
//             // This is because, without include, the filter will include all files
//             // by default. We don't want this behavior.
//             filter.include(matchers == null || matchers.isEmpty() ? List.of("<<!!ET_NOMATCH!!>") : matchers);
//             filter.exclude(excludes == null || excludes.isEmpty() ? List.of() : excludes);
//           }

//         });
//       }

//     });
//   }

//   private static Supplier<FileTree> addDependency(Project proj, NativeLibraryConfig lib) {
//     String config = lib.getConfiguration() == null ? "native_" + lib.getName() : lib.getConfiguration();
//     Configuration cfg = proj.getConfigurations().maybeCreate(config);
//     proj.getDependencies().registerTransform(UnzipTransform.class,
//         new Action<TransformSpec<TransformParameters.None>>() {
//           @Override
//           public void execute(TransformSpec<TransformParameters.None> variantTransform) {
//             variantTransform.getFrom().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ZIP_TYPE);
//             variantTransform.getTo().attribute(ArtifactAttributes.ARTIFACT_FORMAT, DIRECTORY_TYPE);
//           }
//         });
//     if (lib.getMaven() != null) {
//       proj.getDependencies().add(config, lib.getMaven());
//       ArtifactView includeDirs = cfg.getIncoming().artifactView(new Action<ViewConfiguration>() {
//         @Override
//         public void execute(ViewConfiguration viewConfiguration) {
//           viewConfiguration.attributes(new Action<AttributeContainer>() {

//             @Override
//             public void execute(AttributeContainer attributeContainer) {
//               attributeContainer.attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.DIRECTORY_TYPE);
//             }

//           });
//         }
//       });
//       return new Supplier<FileTree>() {
//         @Override
//         public FileTree get() {
//           return proj.fileTree(includeDirs.getFiles().getSingleFile());
//         }
//       };
//     } else if (lib.getFile() != null && lib.getFile().isDirectory()) {
//       // File is a directory
//       return new Supplier<FileTree>() {
//         @Override
//         public FileTree get() {
//           return proj.fileTree(lib.getFile());
//         }
//       };
//     } else if (lib.getFile() != null && lib.getFile().isFile()) {
//       return new Supplier<FileTree>() {
//         @Override
//         public FileTree get() {
//           return proj.getRootProject().zipTree(lib.getFile());
//         }
//       };
//     } else {
//       throw new GradleException("No target defined for dependency " + lib.getName() + " (maven=" + lib.getMaven()
//           + " file=" + lib.getFile() + ")");
//     }
//   }

//   private void handleNewCombinedDependency(CombinedDependencyConfig combined) {
//     String name = combined.getName();
//     String libraryName = combined.getLibraryName();
//     List<String> deps = combined.getDependencies();
//     Set<String> targetPlatforms = combined.getTargetPlatforms();
//     combinedNativeLibraryConfigs.create(name, lib -> {
//       List<String> combinedLibs = lib.getLibs();
//       combinedLibs.addAll(deps);

//       lib.setLibraryName(libraryName);
//       lib.setTargetPlatforms(new HashSet<>(targetPlatforms));
//     });
//   }

//   String[] buildKinds = { "debug", "" };
//   List<String> sharedMatchers = Arrays.asList("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.lib");
//   List<String> runtimeMatchers = Arrays.asList("**/*.so", "**/*.so.*", "**/*.dylib", "**/*.dll");
//   List<String> sharedExcludes = Arrays.asList("**/*.so.debug", "**/*.so.*.debug");
//   List<String> runtimeExcludes = Arrays.asList("**/*.so.debug", "**/*.so.*.debug");
//   List<String> staticMatchers = Arrays.asList("**/*.lib", "**/*.a");

//   private void setCommon(NativeLibraryConfig lib) {
//     lib.setHeaderDirs(new ArrayList<>());
//     lib.setSourceDirs(new ArrayList<>());
//     lib.setStaticMatchers(new ArrayList<>());
//     ArrayList<String> debugMatchers = new ArrayList<>();
//     debugMatchers.add("**/*.pdb");
//     debugMatchers.add("**/*.so.debug");
//     debugMatchers.add("**/*.so.*.debug");
//     lib.setDebugMatchers(debugMatchers);
//     lib.setSharedMatchers(new ArrayList<>());
//     lib.setDynamicMatchers(new ArrayList<>());
//   }

//   private void handleNewDependency(DependencyConfig dependency) {
//     String name = dependency.getName();
//     String config = "native_" + name;
//     String mavenBase = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() + ":";
//     String mavenSuffix = "@" + dependency.getExt();
//     boolean skipMissingPlatform = dependency.isSkipMissingPlatform();

//     boolean createdShared = false;
//     boolean createdStatic = false;

//     if (dependency.getHeaderClassifier() != null) {
//       nativeLibraryConfigs.create(name + "_headers", lib -> {
//         setCommon(lib);
//         lib.setTargetPlatforms(Set.of());
//         lib.getHeaderDirs().add("");
//         lib.setLibraryName(name + "_headers");
//         lib.setMaven(mavenBase + dependency.getHeaderClassifier() + mavenSuffix);
//         lib.setConfiguration(config + "_headers");
//       });
//     }

//     if (dependency.getSourceClassifier() != null) {
//       nativeLibraryConfigs.create(name + "_sources", lib -> {
//         setCommon(lib);
//         lib.setTargetPlatforms(Set.of());
//         lib.getSourceDirs().add("");
//         lib.setLibraryName(name + "_sources");
//         lib.setMaven(mavenBase + dependency.getSourceClassifier() + mavenSuffix);
//         lib.setConfiguration(config + "_sources");
//       });
//     }

//     for (String buildKind : buildKinds) {
//       String buildType = buildKind.contains("debug") ? "debug" : "release";
//       String binaryConfig = config + buildType + "_";

//       for (String platform : dependency.getSharedPlatforms()) {
//         createdShared = true;
//         nativeLibraryConfigs.create(name + "_shared_" + platform + buildType, lib -> {
//           setCommon(lib);
//           lib.setTargetPlatform(platform);
//           lib.setLibraryName(name + "_shared_binaries");
//           lib.setBuildType(buildType);
//           lib.setSharedMatchers(new ArrayList<>(sharedMatchers));
//           lib.setStaticMatchers(new ArrayList<>(staticMatchers));
//           lib.setSharedExcludes(new ArrayList<>(sharedExcludes));
//           lib.getSharedExcludes().addAll(dependency.getSharedExcludes());
//           if (dependency.getSharedUsedAtRuntime()) {
//             lib.setDynamicMatchers(new ArrayList<>(runtimeMatchers));
//             lib.setDynamicExcludes(new ArrayList<>(runtimeExcludes));
//           }
//           lib.setSkipMissingPlatform(skipMissingPlatform);
//           lib.setMaven(mavenBase + platform + buildKind + mavenSuffix);
//           lib.setConfiguration(binaryConfig + "shared_" + platform);
//         });
//       }

//       for (String platform : dependency.getStaticPlatforms()) {
//         createdStatic = true;
//         nativeLibraryConfigs.create(name + "_static_" + platform + buildType, lib -> {
//           setCommon(lib);
//           lib.setTargetPlatform(platform);
//           lib.setLibraryName(name + "_static_binaries");
//           lib.setBuildType(buildType);
//           lib.setSharedMatchers(new ArrayList<>(sharedMatchers));
//           lib.setStaticMatchers(new ArrayList<>(staticMatchers));
//           lib.setSharedExcludes(new ArrayList<>(sharedExcludes));
//           lib.setSkipMissingPlatform(skipMissingPlatform);
//           lib.setMaven(mavenBase + platform + "static" + buildKind + mavenSuffix);
//           lib.setConfiguration(binaryConfig + "static_" + platform);
//         });
//       }
//     }

//     if (createdShared && !dependency.isSkipCombinedDependency()) {
//       combinedNativeLibraryConfigs.create(name + "_shared", lib -> {
//         List<String> combinedLibs = lib.getLibs();
//         combinedLibs.add(name + "_shared_binaries");
//         if (dependency.getHeaderClassifier() != null) {
//           combinedLibs.add(name + "_headers");
//         }
//         if (dependency.getSourceClassifier() != null) {
//           combinedLibs.add(name + "_sources");
//         }

//         lib.setSkipMissingPlatform(skipMissingPlatform);
//         lib.setTargetPlatforms(new HashSet<>(dependency.getSharedPlatforms()));
//       });
//     }

//     if (createdStatic && !dependency.isSkipCombinedDependency()) {
//       combinedNativeLibraryConfigs.create(name + "_static", lib -> {
//         List<String> combinedLibs = lib.getLibs();
//         combinedLibs.add(name + "_static_binaries");
//         if (dependency.getHeaderClassifier() != null) {
//           combinedLibs.add(name + "_headers");
//         }
//         if (dependency.getSourceClassifier() != null) {
//           combinedLibs.add(name + "_sources");
//         }

//         lib.setSkipMissingPlatform(skipMissingPlatform);
//         lib.setTargetPlatforms(new HashSet<>(dependency.getStaticPlatforms()));
//       });
//     }
//   }

  public void setSinglePrintPerPlatform() {
    tcExt.setSinglePrintPerPlatform();
  }

  public NamedDomainObjectContainer<ToolchainDescriptorBase> getToolchainDescriptors() {
    return tcExt.getToolchainDescriptors();
  }

  void toolchainDescriptors(final Action<? super NamedDomainObjectContainer<ToolchainDescriptorBase>> closure) {
    closure.execute(tcExt.getToolchainDescriptors());
  }

  public NamedDomainObjectContainer<CrossCompilerConfiguration> getCrossCompilers() {
    return tcExt.getCrossCompilers();
  }

  void crossCompilers(final Action<? super NamedDomainObjectContainer<CrossCompilerConfiguration>> closure) {
    closure.execute(tcExt.getCrossCompilers());
  }

  public NamedDomainObjectContainer<PlatformConfig> getPlatformConfigs() {
    return platformConfigs;
  }

  void platformConfigs(final Action<? super NamedDomainObjectContainer<PlatformConfig>> closure) {
    closure.execute(platformConfigs);
  }

  public NamedDomainObjectContainer<ExportsConfig> getExportsConfigs() {
    return exportsConfigs;
  }

  // void exportsConfigs(final Action<? super NamedDomainObjectContainer<ExportsConfig>> closure) {
  //   closure.execute(exportsConfigs);
  // }

  // public NamedDomainObjectContainer<DependencyConfig> getDependencyConfigs() {
  //   return dependencyConfigs;
  // }

  // void dependencyConfigs(final Action<? super NamedDomainObjectContainer<DependencyConfig>> closure) {
  //   closure.execute(dependencyConfigs);
  // }

  // public NamedDomainObjectContainer<CombinedDependencyConfig> getCombinedDependencyConfigs() {
  //   return combinedDependencyConfigs;
  // }

  // void combinedDependencyConfigs(final Action<? super NamedDomainObjectContainer<CombinedDependencyConfig>> closure) {
  //   closure.execute(combinedDependencyConfigs);
  // }

  public NamedDomainObjectContainer<PrivateExportsConfig> getPrivateExportsConfigs() {
    return privateExportsConfigs;
  }

  void privateExportsConfigs(final Action<? super NamedDomainObjectContainer<PrivateExportsConfig>> closure) {
    closure.execute(privateExportsConfigs);
  }

  public String getPlatformPath(NativeBinarySpec binary) {
    PlatformConfig platform = platformConfigs.findByName(binary.getTargetPlatform().getName());
    if (platform == null) {
      return binary.getTargetPlatform().getOperatingSystem().getName() + "/"
          + binary.getTargetPlatform().getArchitecture().getName();
    }
    return platform.getPlatformPath().get();
  }

  public String getDependencyClassifier(NativeBinarySpec binary, boolean isStaticDep) {
    String classifierBase = binary.getTargetPlatform().getName();
    if (isStaticDep) {
      classifierBase += "static";
    }
    if (!binary.getBuildType().getName().equals("release")) {
      classifierBase += binary.getBuildType().getName();
    }
    return classifierBase;
  }

  public String getPublishClassifier(NativeBinarySpec binary) {
    String classifierBase = binary.getTargetPlatform().getName();
    if (binary instanceof StaticLibraryBinarySpec) {
      classifierBase += "static";
    }
    if (!binary.getBuildType().getName().contains("release")) {
      classifierBase += binary.getBuildType().getName();
    }
    return classifierBase;
  }

  public void useRequiredLibrary(VariantComponentSpec component, String... libraries) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      useRequiredLibrary((NativeBinarySpec) binary, libraries);
    });
  }

  public void useRequiredLibrary(NativeBinarySpec binary, String... libraries) {
    for (String library : libraries) {
      DelegatedDependencySet dds = objectFactory.newInstance(DelegatedDependencySet.class, library, dependencyContainer, true, binary);
      binary.lib(dds);
    }

    // FrcNativeBinaryExtension frcBin = nativeBinaryExt.maybeCreate(binary.getName());
    // frcBin.getDependencies().addAll(Arrays.asList(libraries));
  }

  public void useOptionalLibrary(VariantComponentSpec component, String... libraries) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      useOptionalLibrary((NativeBinarySpec) binary, libraries);
    });
  }

  // public FrcNativeBinaryExtension getBinaryExtension(NativeBinarySpec binary) {
  //   return nativeBinaryExt.findByName(binary.getName());
  // }

  public void useOptionalLibrary(NativeBinarySpec binary, String... libraries) {
    for (String library : libraries) {
      DelegatedDependencySet dds = objectFactory.newInstance(DelegatedDependencySet.class, library, dependencyContainer, false, binary);
      binary.lib(dds);
    }
  }

  public void useAllPlatforms(PlatformAwareComponentSpec component) {
    for (String platform : platformsToConfigure) {
      component.targetPlatform(platform);
    }
  }

  public void usePlatform(PlatformAwareComponentSpec component, String platform) {
    if (platformsToConfigure.contains(platform)) {
      component.targetPlatform(platform);
    }
  }

  // Internal, used from the model to add the platforms
  public void addPlatformsToConfigure(PlatformContainer platforms) {
    List<String> tmpList = new ArrayList<>();
    for (Platform platform : platforms) {
      tmpList.add(platform.getName());
    }
    boolean only = false;

    for (int i = 0; i < tmpList.size(); i++) {
      String platform = tmpList.get(i);
      if (project.hasProperty("skip" + platform)) {
        tmpList.remove(i);
        i--;
        continue;
      }
      if (project.hasProperty("only" + platform)) {
        only = true;
        platformsToConfigure.add(platform);
      }
    }

    if (!only) {
      platformsToConfigure.addAll(tmpList);
    }

    if (!project.hasProperty("buildwin32") && NativePlatforms.desktopArch().equals("x86-64")) {
      platformsToConfigure.remove("windowsx86");
    }
  }

  public void configurePlatform(String name, Action<? super PlatformConfig> action) {
    getPlatformConfigs().getByName(name, action);
  }

  public void usePlatformArguments(NativeBinarySpec binary) {
    String targetName = binary.getTargetPlatform().getName();
    PlatformConfig config = this.getPlatformConfigs().findByName(targetName);
    if (config == null) {
      return;
    }

    boolean isDebug = binary.getBuildType().getName().contains("debug");
    config.getCppCompiler().apply(binary.getCppCompiler(), isDebug);
    config.getLinker().apply(binary.getLinker(), isDebug);
    config.getcCompiler().apply(binary.getcCompiler(), isDebug);
    config.getAssembler().apply(binary.getAssembler(), isDebug);
    config.getObjcppCompiler().apply(binary.getObjcppCompiler(), isDebug);
    config.getObjcCompiler().apply(binary.getObjcCompiler(), isDebug);
  }

  public void usePlatformArguments(PlatformAwareComponentSpec component) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      usePlatformArguments(binary);
    });
  }

  public void addWpiNativeUtils() {
    project.getPluginManager().apply(WPINativeUtils.class);
  }

  private WPINativeUtilsExtension wpiNativeUtilsExtension;

  void addWpiExtension() {
    wpiNativeUtilsExtension = objectFactory.newInstance(WPINativeUtilsExtension.class, this);
  }

  public WPINativeUtilsExtension getWpi() {
    return wpiNativeUtilsExtension;
  }

  public void wpi(Action<WPINativeUtilsExtension> action) {
    action.execute(wpiNativeUtilsExtension);
  }

  public void withRoboRIO() {
    project.getPluginManager().apply(RoboRioToolchainPlugin.class);
  }

  public void withRaspbian() {
    project.getPluginManager().apply(RaspbianToolchainPlugin.class);
  }

  public void withBionic() {
    project.getPluginManager().apply(BionicToolchainPlugin.class);
  }

  public void excludeBinariesFromStrip(VariantComponentSpec component) {
    component.getBinaries().withType(NativeBinarySpec.class).all(bin -> {
      tcExt.addStripExcludeComponentsForPlatform(bin.getTargetPlatform().getName(), component.getName());
    });
  }

  public void excludeBinaryFromStrip(NativeBinarySpec binary) {
    tcExt.addStripExcludeComponentsForPlatform(binary.getTargetPlatform().getName(), binary.getComponent().getName());
  }

  public void enableSourceLink() {
    if (OperatingSystem.current().isWindows()) {
      project.getPluginManager().apply(SourceLinkPlugin.class);
    }
  }

  private boolean skipInstallPdb = false;

  public boolean isSkipInstallPdb() {
    return skipInstallPdb;
  }

  public void setSkipInstallPdb(boolean skip) {
    skipInstallPdb = skip;
  }
}
