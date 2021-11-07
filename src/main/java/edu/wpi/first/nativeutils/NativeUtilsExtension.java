package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import edu.wpi.first.nativeutils.dependencies.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.CombinedIgnoreMissingPlatformNativeDependency;
import edu.wpi.first.nativeutils.dependencies.CombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.DelegatedDependencySet;
import edu.wpi.first.nativeutils.dependencies.FastDownloadDependencySet;
import edu.wpi.first.nativeutils.dependencies.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.WPISharedMavenDependency;
import edu.wpi.first.nativeutils.dependencies.WPIStaticMavenDependency;
import edu.wpi.first.nativeutils.exports.DefaultExportsConfig;
import edu.wpi.first.nativeutils.exports.ExportsConfig;
import edu.wpi.first.nativeutils.exports.PrivateExportsConfig;
import edu.wpi.first.nativeutils.platforms.DefaultPlatformConfig;
import edu.wpi.first.nativeutils.platforms.PlatformConfig;
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

  private final NamedDomainObjectContainer<PrivateExportsConfig> privateExportsConfigs;

  private final TaskProvider<PrintNativeDependenciesTask> printNativeDependenciesTask;

  public TaskProvider<PrintNativeDependenciesTask> getPrintNativeDependenciesTask() {
    return printNativeDependenciesTask;
  }

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

    platformConfigs = objectFactory.domainObjectContainer(PlatformConfig.class, name -> {
      return (PlatformConfig)objectFactory.newInstance(DefaultPlatformConfig.class, name);
    });

    privateExportsConfigs = objectFactory.domainObjectContainer(PrivateExportsConfig.class, name -> {
      PrivateExportsConfig exports = objectFactory.newInstance(PrivateExportsConfig.class, name);
      exports.getPerformStripAllSymbols().convention(false);
      return exports;
    });

    project.afterEvaluate(proj -> {
      for (PlatformConfig config : platformConfigs) {
        if (config.getPlatformPath() == null) {
          throw new GradleException("Platform Path cannot be null: " + config.getName());
        }
      }
    });

    printNativeDependenciesTask = project.getTasks().register("printNativeDependencyGraph", PrintNativeDependenciesTask.class);
  }

  public ExtensiblePolymorphicDomainObjectContainer<NativeDependency> getNativeDependencyContainer() {
    return dependencyContainer;
  }

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

  private Map<NativeBinarySpec, FastDownloadDependencySet> depSetMap = new HashMap<>();
  private FastDownloadDependencySet getFastDepSet(NativeBinarySpec binary) {
    FastDownloadDependencySet fastDepSet = depSetMap.get(binary);
    if (fastDepSet == null) {
      fastDepSet = new FastDownloadDependencySet(project);
      depSetMap.put(binary, fastDepSet);
      binary.lib(fastDepSet);
    }
    return fastDepSet;
  }

  public void useRequiredLibrary(NativeBinarySpec binary, String... libraries) {
    FastDownloadDependencySet fastDepSet = getFastDepSet(binary);

    for (String library : libraries) {
      DelegatedDependencySet dds = objectFactory.newInstance(DelegatedDependencySet.class, library, dependencyContainer, true, binary, fastDepSet);
      binary.lib(dds);
    }
  }

  public void useOptionalLibrary(VariantComponentSpec component, String... libraries) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      useOptionalLibrary((NativeBinarySpec) binary, libraries);
    });
  }

  public void useOptionalLibrary(NativeBinarySpec binary, String... libraries) {
    FastDownloadDependencySet fastDepSet = getFastDepSet(binary);

    for (String library : libraries) {
      DelegatedDependencySet dds = objectFactory.newInstance(DelegatedDependencySet.class, library, dependencyContainer, false, binary, fastDepSet);
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
