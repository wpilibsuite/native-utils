package edu.wpi.first.nativeutils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;

import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class WPINativeExtension {
  private NativeUtilsExtension nativeExt;

  public List<String> windowsCompilerArgs = Collections
      .unmodifiableList(Arrays.asList("/EHsc", "/DNOMINMAX", "/Zi", "/FS", "/Zc:inline", "/MP4"));
  public List<String> windowsCCompilerArgs = Collections.unmodifiableList(Arrays.asList("/Zi", "/FS", "/Zc:inline"));
  public List<String> windowsReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("/O2", "/MD"));
  public List<String> windowsDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("/Od", "/MDd"));
  public List<String> windowsLinkerArgs = Collections.unmodifiableList(Arrays.asList("/DEBUG:FULL"));
  public List<String> windowsReleaseLinkerArgs = Collections.unmodifiableList(Arrays.asList("/OPT:REF", "/OPT:ICF"));

  public List<String> linuxCrossCompilerArgs = Collections.unmodifiableList(
      Arrays.asList("-std=c++14", "-Wformat=2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi", "-g",
          "-Wno-unused-parameter", "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
  public List<String> linuxCrossCCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Wformat=2", "-Wall", "-Wextra",
      "-Werror", "-pedantic", "-Wno-psabi", "-g", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
  public List<String> linuxCrossLinkerArgs = Collections.unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl"));
  public List<String> linuxCrossReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
  public List<String> linuxCrossDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Og"));

  public List<String> linuxCompilerArgs = Collections.unmodifiableList(
      Arrays.asList("-std=c++14", "-Wformat=2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi", "-g",
          "-Wno-unused-parameter", "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
  public List<String> linuxCCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Wformat=2", "-Wall", "-Wextra",
      "-Werror", "-pedantic", "-Wno-psabi", "-g", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
  public List<String> linuxLinkerArgs = Collections.unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl"));
  public List<String> linuxReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
  public List<String> linuxDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O0"));

  public List<String> macCompilerArgs = Collections.unmodifiableList(Arrays.asList("-std=c++14", "-Wall", "-Wextra", "-Werror",
      "-pedantic-errors", "-fPIC", "-g", "-Wno-unused-parameter", "-Wno-error=deprecated-declarations",
      "-Wno-missing-field-initializers", "-Wno-unused-private-field", "-Wno-unused-const-variable", "-pthread"));
  public List<String> macCCompilerArgs = Collections
      .unmodifiableList(Arrays.asList("-Wall", "-Wextra", "-Werror", "-pedantic-errors", "-fPIC", "-g",
          "-Wno-unused-parameter", "-Wno-missing-field-initializers", "-Wno-unused-private-field"));
  public List<String> macObjCppCompilerArgs = Collections.unmodifiableList(
      Arrays.asList("-std=c++14", "-stdlib=libc++", "-fobjc-arc", "-g", "-fPIC", "-Wall", "-Wextra", "-Werror"));
  public List<String> macReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
  public List<String> macDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O0"));
  public List<String> macLinkerArgs = Collections.unmodifiableList(Arrays.asList("-framework", "CoreFoundation", "-framework",
      "AVFoundation", "-framework", "Foundation", "-framework", "CoreMedia", "-framework", "CoreVideo"));

  @Inject
  public WPINativeExtension(NativeUtilsExtension nativeExt) {
    this.nativeExt = nativeExt;

    Collections.unmodifiableList(Arrays.asList(""));

    PlatformConfig windowsx86_64 = nativeExt.getPlatformConfigs().create("windowsx86-64");
    PlatformConfig windowsx86 = nativeExt.getPlatformConfigs().create("windowsx86");
    PlatformConfig linuxx86_64 = nativeExt.getPlatformConfigs().create("linuxx86-64");
    PlatformConfig osxx86_64 = nativeExt.getPlatformConfigs().create("osxx86-64");
    PlatformConfig linuxathena = nativeExt.getPlatformConfigs().create("linuxathena");
    PlatformConfig linuxraspbian = nativeExt.getPlatformConfigs().create("linuxraspbian");

    linuxathena.setPlatformPath("linux/athena");
    linuxathena.getCppCompiler().getArgs().addAll(linuxCrossCompilerArgs);
    linuxathena.getcCompiler().getArgs().addAll(linuxCrossCCompilerArgs);
    linuxathena.getLinker().getArgs().addAll(linuxCrossLinkerArgs);
    linuxathena.getCppCompiler().getDebugArgs().addAll(linuxCrossDebugCompilerArgs);
    linuxathena.getCppCompiler().getReleaseArgs().addAll(linuxCrossReleaseCompilerArgs);


    linuxraspbian.setPlatformPath("linux/raspbian");
    linuxraspbian.getCppCompiler().getArgs().addAll(linuxCrossCompilerArgs);
    linuxraspbian.getcCompiler().getArgs().addAll(linuxCrossCCompilerArgs);
    linuxraspbian.getLinker().getArgs().addAll(linuxCrossLinkerArgs);
    linuxraspbian.getCppCompiler().getDebugArgs().addAll(linuxCrossDebugCompilerArgs);
    linuxraspbian.getCppCompiler().getReleaseArgs().addAll(linuxCrossReleaseCompilerArgs);

    windowsx86_64.setPlatformPath("windows/x86-64");
    windowsx86_64.getCppCompiler().getArgs().addAll(windowsCompilerArgs);
    windowsx86_64.getcCompiler().getArgs().addAll(windowsCCompilerArgs);
    windowsx86_64.getLinker().getArgs().addAll(windowsLinkerArgs);
    windowsx86_64.getLinker().getReleaseArgs().addAll(windowsReleaseLinkerArgs);
    windowsx86_64.getCppCompiler().getDebugArgs().addAll(windowsDebugCompilerArgs);
    windowsx86_64.getCppCompiler().getReleaseArgs().addAll(windowsReleaseCompilerArgs);

    windowsx86.setPlatformPath("windows/x86");
    windowsx86.getCppCompiler().getArgs().addAll(windowsCompilerArgs);
    windowsx86.getcCompiler().getArgs().addAll(windowsCCompilerArgs);
    windowsx86.getLinker().getArgs().addAll(windowsLinkerArgs);
    windowsx86.getLinker().getReleaseArgs().addAll(windowsReleaseLinkerArgs);
    windowsx86.getCppCompiler().getDebugArgs().addAll(windowsDebugCompilerArgs);
    windowsx86.getCppCompiler().getReleaseArgs().addAll(windowsReleaseCompilerArgs);

    linuxx86_64.setPlatformPath("linux/x86-64");
    linuxx86_64.getCppCompiler().getArgs().addAll(linuxCompilerArgs);
    linuxx86_64.getcCompiler().getArgs().addAll(linuxCCompilerArgs);
    linuxx86_64.getLinker().getArgs().addAll(linuxLinkerArgs);
    linuxx86_64.getCppCompiler().getDebugArgs().addAll(linuxDebugCompilerArgs);
    linuxx86_64.getCppCompiler().getReleaseArgs().addAll(linuxReleaseCompilerArgs);

    osxx86_64.setPlatformPath("osx/x86-64");
    osxx86_64.getCppCompiler().getArgs().addAll(macCompilerArgs);
    osxx86_64.getcCompiler().getArgs().addAll(macCCompilerArgs);
    osxx86_64.getLinker().getArgs().addAll(macLinkerArgs);
    osxx86_64.getCppCompiler().getDebugArgs().addAll(macDebugCompilerArgs);
    osxx86_64.getCppCompiler().getReleaseArgs().addAll(macReleaseCompilerArgs);
    osxx86_64.getObjcppCompiler().getArgs().addAll(macObjCppCompilerArgs);


  }

  public void configurePlatform(String name, Action<? super PlatformConfig> action) {
    nativeExt.configurePlatform(name, action);
  }
}
