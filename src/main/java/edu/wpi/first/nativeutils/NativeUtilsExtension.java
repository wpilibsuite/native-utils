package edu.wpi.first.nativeutils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformAwareComponentSpec;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.embeddedtools.nativedeps.DelegatedDependencySet;
import edu.wpi.first.embeddedtools.nativedeps.DependencySpecExtension;
import edu.wpi.first.nativeutils.configs.CombinedDependencyConfig;
import edu.wpi.first.nativeutils.configs.DependencyConfig;
import edu.wpi.first.nativeutils.configs.ExportsConfig;
import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.configs.PrivateExportsConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultCombinedDependencyConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultDependencyConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultExportsConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultPlatformConfig;
import edu.wpi.first.nativeutils.configs.impl.DefaultPrivateExportsConfig;
import edu.wpi.first.nativeutils.rules.GitLinkRules;
import edu.wpi.first.nativeutils.tasks.SourceLinkGenerationTask;
import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.toolchain.ToolchainDescriptorBase;
import edu.wpi.first.toolchain.ToolchainExtension;
import edu.wpi.first.toolchain.bionic.BionicToolchainPlugin;
import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.raspbian.RaspbianToolchainPlugin;
import edu.wpi.first.toolchain.roborio.RoboRioToolchainPlugin;
import edu.wpi.first.toolchain.xenial.XenialToolchainPlugin;

public class NativeUtilsExtension {

  private final NamedDomainObjectContainer<PlatformConfig> platformConfigs;

  private final NamedDomainObjectContainer<ExportsConfig> exportsConfigs;

  private final NamedDomainObjectContainer<DependencyConfig> dependencyConfigs;

  private final NamedDomainObjectContainer<CombinedDependencyConfig> combinedDependencyConfigs;

  private final NamedDomainObjectContainer<PrivateExportsConfig> privateExportsConfigs;

  private final Project project;

  private DependencySpecExtension dse = null;

  private final List<String> platformsToConfigure = new ArrayList<>();

  private final ObjectFactory objectFactory;

  private final ToolchainExtension tcExt;

  @Inject
  public NativeUtilsExtension(Project project, ToolchainExtension tcExt) {
    this.project = project;
    this.tcExt = tcExt;
    this.objectFactory = project.getObjects();

    exportsConfigs = project.container(ExportsConfig.class, name -> {
      return objectFactory.newInstance(DefaultExportsConfig.class, name);
    });

    dependencyConfigs = project.container(DependencyConfig.class, name -> {
      return objectFactory.newInstance(DefaultDependencyConfig.class, name);
    });

    platformConfigs = project.container(PlatformConfig.class, name -> {
      return objectFactory.newInstance(DefaultPlatformConfig.class, name);
    });

    combinedDependencyConfigs = project.container(CombinedDependencyConfig.class, name -> {
      return objectFactory.newInstance(DefaultCombinedDependencyConfig.class, name);
    });

    privateExportsConfigs = project.container(PrivateExportsConfig.class, name -> {
      return objectFactory.newInstance(DefaultPrivateExportsConfig.class, name);
    });

    project.afterEvaluate(proj -> {
      for (PlatformConfig config : platformConfigs) {
        if (config.getPlatformPath() == null) {
          throw new GradleException("Platform Path cannot be null: " + config.getName());
        }
      }
    });

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

  void exportsConfigs(final Action<? super NamedDomainObjectContainer<ExportsConfig>> closure) {
    closure.execute(exportsConfigs);
  }

  public NamedDomainObjectContainer<DependencyConfig> getDependencyConfigs() {
    return dependencyConfigs;
  }

  void dependencyConfigs(final Action<? super NamedDomainObjectContainer<DependencyConfig>> closure) {
    closure.execute(dependencyConfigs);
  }

  public NamedDomainObjectContainer<CombinedDependencyConfig> getCombinedDependencyConfigs() {
    return combinedDependencyConfigs;
  }

  void combinedDependencyConfigs(final Action<? super NamedDomainObjectContainer<CombinedDependencyConfig>> closure) {
    closure.execute(combinedDependencyConfigs);
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
    return platform.getPlatformPath();
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
    if (dse == null) {
      dse = project.getExtensions().getByType(DependencySpecExtension.class);
    }
    for (String library : libraries) {
      binary.lib(new DelegatedDependencySet(library, binary, dse, false));
    }
  }

  public void useOptionalLibrary(VariantComponentSpec component, String... libraries) {
    component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
      useOptionalLibrary((NativeBinarySpec) binary, libraries);
    });
  }

  public void useOptionalLibrary(NativeBinarySpec binary, String... libraries) {
    if (dse == null) {
      dse = project.getExtensions().getByType(DependencySpecExtension.class);
    }
    for (String library : libraries) {
      binary.lib(new DelegatedDependencySet(library, binary, dse, true));
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

  public void withXenial() {
    project.getPluginManager().apply(XenialToolchainPlugin.class);
  }

  public void excludeBinariesFromStrip(VariantComponentSpec component) {
    component.getBinaries().withType(NativeBinarySpec.class).all(bin -> {
      tcExt.addStripExcludeComponentsForPlatform(bin.getTargetPlatform().getName(), component.getName());
    });
  }

  public void excludeBinaryFromStrip(NativeBinarySpec binary) {
    tcExt.addStripExcludeComponentsForPlatform(binary.getTargetPlatform().getName(), binary.getComponent().getName());
  }

  private File getGitDir(File currentDir) {
    if (new File(currentDir, ".git").exists()) {
      return currentDir;
    }

    File parentFile = currentDir.getParentFile();

    if (parentFile == null) {
      return null;
    }

    return parentFile;
  }

  TaskProvider<SourceLinkGenerationTask> sourceLinkTask;

  public TaskProvider<SourceLinkGenerationTask> getSourceLinkTask() {
    return sourceLinkTask;
  }

  public void enableSourceLink() {
    if (OperatingSystem.current().isWindows()) {
      String extractTaskName = "generateSourceLinkFile";
      try {
        sourceLinkTask = project.getRootProject().getTasks().named(extractTaskName, SourceLinkGenerationTask.class);
        project.getPluginManager().apply(GitLinkRules.class);
      } catch (UnknownTaskException notfound) {
        File gitDir = getGitDir(project.getRootProject().getRootDir());
        if (gitDir == null) {
          System.out.println("No .git directory was found in" + project.getRootProject().getRootDir().toString()
              + "or any parent directories of that directory.");
          System.out.println("SourceLink generation skipped");
        } else {
          sourceLinkTask = project.getRootProject().getTasks().register(extractTaskName, SourceLinkGenerationTask.class,
              gitDir);

          project.getPluginManager().apply(GitLinkRules.class);
        }
      }
    }
  }
}
