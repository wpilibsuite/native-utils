package edu.wpi.first.nativeutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import edu.wpi.first.nativeutils.configs.PlatformConfig;
import edu.wpi.first.nativeutils.dependencies.configs.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.configs.CombinedIgnoreMissingPlatformNativeDependency;
import edu.wpi.first.nativeutils.dependencies.configs.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.configs.WPISharedMavenDependency;
import edu.wpi.first.nativeutils.dependencies.configs.WPIStaticMavenDependency;

public class WPINativeUtilsExtension {
    private NativeUtilsExtension nativeExt;

    public static class DefaultArguments {

        public final List<String> windowsCompilerArgs = List.of("/EHsc", "/FS", "/Zc:inline", "/wd4244", "/wd4267",
                "/wd4146", "/wd4996", "/Zc:throwingNew", "/D_CRT_SECURE_NO_WARNINGS", "/std:c++17", "/permissive-",
                "/bigobj");
        public final List<String> windowsCCompilerArgs = List.of("/FS", "/Zc:inline", "/D_CRT_SECURE_NO_WARNINGS");
        public final List<String> windowsReleaseCompilerArgs = List.of("/O2", "/MD");
        public final List<String> windowsDebugCompilerArgs = List.of("/Od", "/MDd");
        public final List<String> windowsLinkerArgs = List.of("/DEBUG:FULL", "/PDBALTPATH:%_PDB%");
        public final List<String> windowsReleaseLinkerArgs = List.of("/OPT:REF", "/OPT:ICF");

        public final String windowsSymbolArg = "/Zi";

        public final List<String> windowsWarningArgs = List.of("/W3");
        public final List<String> windowsWarningsAsErrorsArgs = List.of("/WX");

        public final List<String> unixWarningArgs = List.of("-Wall", "-Wextra");
        public final List<String> unixWarningsAsErrorsArgs = List.of("-Werror");

        public final String unixSymbolArg = "-g";

        public final List<String> linuxCrossCompilerArgs = List.of("-std=c++17", "-Wformat=2", "-pedantic",
                "-Wno-psabi", "-Wno-unused-parameter", "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic",
                "-pthread");
        public final List<String> linuxCrossCCompilerArgs = List.of("-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread");
        public final List<String> linuxCrossLinkerArgs = List.of("-rdynamic", "-pthread", "-ldl", "-latomic");
        public final List<String> linuxCrossReleaseCompilerArgs = List.of("-O2");
        public final List<String> linuxCrossDebugCompilerArgs = List.of("-Og");

        public final List<String> linuxCompilerArgs = List.of("-std=c++17", "-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread");
        public final List<String> linuxCCompilerArgs = List.of("-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread");
        public final List<String> linuxLinkerArgs = List.of("-rdynamic", "-pthread", "-ldl", "-latomic");
        public final List<String> linuxReleaseCompilerArgs = List.of("-O2");
        public final List<String> linuxDebugCompilerArgs = List.of("-O0");

        public final List<String> macCompilerArgs = List.of("-std=c++17", "-pedantic", "-fPIC", "-Wno-unused-parameter",
                "-Wno-error=deprecated-declarations", "-Wno-missing-field-initializers", "-Wno-unused-private-field",
                "-Wno-unused-const-variable", "-Wno-error=c11-extensions", "-pthread");
        public final List<String> macCCompilerArgs = List.of("-pedantic", "-fPIC", "-Wno-unused-parameter",
                "-Wno-missing-field-initializers", "-Wno-unused-private-field");
        public final List<String> macObjcppCompilerArgs = List.of("-std=c++17", "-stdlib=libc++", "-fobjc-weak",
                "-fobjc-arc", "-fPIC");
        public final List<String> macObjcCompilerArgs = List.of("-fobjc-weak", "-fobjc-arc", "-fPIC");
        public final List<String> macReleaseCompilerArgs = List.of("-O2");
        public final List<String> macDebugCompilerArgs = List.of("-O0");
        public final List<String> macLinkerArgs = List.of("-framework", "CoreFoundation", "-framework", "AVFoundation",
                "-framework", "Foundation", "-framework", "CoreMedia", "-framework", "CoreVideo");
    }

    public static class Platforms {
        public final String roborio = "linuxathena";
        public final String raspbian = "linuxraspbian";
        public final String windowsx64 = "windowsx86-64";
        public final String windowsx86 = "windowsx86";
        public final String osxx64 = "osxx86-64";
        public final String linuxx64 = "linuxx86-64";
        public final String aarch64bionic = "linuxaarch64bionic";
        public final String aarch64xenial = "linuxaarch64xenial";
        public final List<String> allPlatforms = List.of(roborio, raspbian, aarch64bionic, aarch64xenial, windowsx64,
                windowsx86, osxx64, linuxx64);
        public final List<String> desktopPlatforms = List.of(windowsx64, windowsx86, osxx64, linuxx64);
    }

    public final Platforms platforms;

    public final DefaultArguments defaultArguments;

    private final Map<String, PlatformConfig> windowsPlatforms = new HashMap<>();
    private final Map<String, PlatformConfig> unixPlatforms = new HashMap<>();

    public void addLinuxCrossArgs(PlatformConfig platform) {
        platform.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerArgs);
        platform.getcCompiler().getArgs().addAll(defaultArguments.linuxCrossCCompilerArgs);
        platform.getLinker().getArgs().addAll(defaultArguments.linuxCrossLinkerArgs);
        platform.getCppCompiler().getDebugArgs().addAll(defaultArguments.linuxCrossDebugCompilerArgs);
        platform.getCppCompiler().getReleaseArgs().addAll(defaultArguments.linuxCrossReleaseCompilerArgs);
        platform.getcCompiler().getDebugArgs().addAll(defaultArguments.linuxCrossDebugCompilerArgs);
        platform.getcCompiler().getReleaseArgs().addAll(defaultArguments.linuxCrossReleaseCompilerArgs);
        platform.getCppCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
        platform.getcCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
    }

    public void addWindowsArgs(PlatformConfig platform) {
        platform.getCppCompiler().getArgs().addAll(defaultArguments.windowsCompilerArgs);
        platform.getcCompiler().getArgs().addAll(defaultArguments.windowsCCompilerArgs);
        platform.getLinker().getArgs().addAll(defaultArguments.windowsLinkerArgs);
        platform.getLinker().getReleaseArgs().addAll(defaultArguments.windowsReleaseLinkerArgs);
        platform.getCppCompiler().getDebugArgs().addAll(defaultArguments.windowsDebugCompilerArgs);
        platform.getCppCompiler().getReleaseArgs().addAll(defaultArguments.windowsReleaseCompilerArgs);
        platform.getcCompiler().getDebugArgs().addAll(defaultArguments.windowsDebugCompilerArgs);
        platform.getcCompiler().getReleaseArgs().addAll(defaultArguments.windowsReleaseCompilerArgs);
        platform.getCppCompiler().getDebugArgs().add(defaultArguments.windowsSymbolArg);
        platform.getcCompiler().getDebugArgs().add(defaultArguments.windowsSymbolArg);
    }

    public void addLinuxArgs(PlatformConfig platform) {
        platform.getCppCompiler().getArgs().addAll(defaultArguments.linuxCompilerArgs);
        platform.getcCompiler().getArgs().addAll(defaultArguments.linuxCCompilerArgs);
        platform.getLinker().getArgs().addAll(defaultArguments.linuxLinkerArgs);
        platform.getCppCompiler().getDebugArgs().addAll(defaultArguments.linuxDebugCompilerArgs);
        platform.getCppCompiler().getReleaseArgs().addAll(defaultArguments.linuxReleaseCompilerArgs);
        platform.getcCompiler().getDebugArgs().addAll(defaultArguments.linuxDebugCompilerArgs);
        platform.getcCompiler().getReleaseArgs().addAll(defaultArguments.linuxReleaseCompilerArgs);
        platform.getCppCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
        platform.getcCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
    }

    public void addMacArgs(PlatformConfig platform) {
        platform.getCppCompiler().getArgs().addAll(defaultArguments.macCompilerArgs);
        platform.getcCompiler().getArgs().addAll(defaultArguments.macCCompilerArgs);
        platform.getLinker().getArgs().addAll(defaultArguments.macLinkerArgs);
        platform.getCppCompiler().getDebugArgs().addAll(defaultArguments.macDebugCompilerArgs);
        platform.getCppCompiler().getReleaseArgs().addAll(defaultArguments.macReleaseCompilerArgs);
        platform.getObjcCompiler().getArgs().addAll(defaultArguments.macObjcCompilerArgs);
        platform.getObjcppCompiler().getArgs().addAll(defaultArguments.macObjcppCompilerArgs);

        platform.getcCompiler().getDebugArgs().addAll(defaultArguments.macDebugCompilerArgs);
        platform.getcCompiler().getReleaseArgs().addAll(defaultArguments.macReleaseCompilerArgs);

        platform.getObjcppCompiler().getDebugArgs().addAll(defaultArguments.macDebugCompilerArgs);
        platform.getObjcppCompiler().getReleaseArgs().addAll(defaultArguments.macReleaseCompilerArgs);

        platform.getObjcCompiler().getDebugArgs().addAll(defaultArguments.macDebugCompilerArgs);
        platform.getObjcCompiler().getReleaseArgs().addAll(defaultArguments.macReleaseCompilerArgs);

        platform.getCppCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
        platform.getcCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
        platform.getObjcCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
        platform.getObjcppCompiler().getDebugArgs().add(defaultArguments.unixSymbolArg);
    }

    private final ObjectFactory objects;

    @Inject
    public WPINativeUtilsExtension(NativeUtilsExtension nativeExt, ObjectFactory objects) {
        this.nativeExt = nativeExt;
        this.objects = objects;

        this.platforms = objects.newInstance(Platforms.class);
        defaultArguments = objects.newInstance(DefaultArguments.class);

        PlatformConfig windowsx86_64 = nativeExt.getPlatformConfigs().create(platforms.windowsx64);
        PlatformConfig windowsx86 = nativeExt.getPlatformConfigs().create(platforms.windowsx86);
        windowsPlatforms.put(platforms.windowsx64, windowsx86_64);
        windowsPlatforms.put(platforms.windowsx86, windowsx86);
        PlatformConfig linuxx86_64 = nativeExt.getPlatformConfigs().create(platforms.linuxx64);
        PlatformConfig osxx86_64 = nativeExt.getPlatformConfigs().create(platforms.osxx64);
        PlatformConfig linuxathena = nativeExt.getPlatformConfigs().create(platforms.roborio);
        PlatformConfig linuxraspbian = nativeExt.getPlatformConfigs().create(platforms.raspbian);
        PlatformConfig linuxbionic = nativeExt.getPlatformConfigs().create(platforms.aarch64bionic);
        PlatformConfig linuxxenial = nativeExt.getPlatformConfigs().create(platforms.aarch64xenial);
        unixPlatforms.put(platforms.linuxx64, linuxx86_64);
        unixPlatforms.put(platforms.osxx64, osxx86_64);
        unixPlatforms.put(platforms.raspbian, linuxraspbian);
        unixPlatforms.put(platforms.roborio, linuxathena);
        unixPlatforms.put(platforms.aarch64bionic, linuxbionic);
        unixPlatforms.put(platforms.aarch64xenial, linuxxenial);

        linuxathena.getPlatformPath().set("linux/athena");
        addLinuxCrossArgs(linuxathena);

        linuxraspbian.getPlatformPath().set("linux/raspbian");
        addLinuxCrossArgs(linuxraspbian);

        linuxbionic.getPlatformPath().set("linux/aarch64bionic");
        addLinuxCrossArgs(linuxbionic);

        linuxxenial.getPlatformPath().set("linux/aarch64xenial");
        addLinuxCrossArgs(linuxxenial);

        windowsx86_64.getPlatformPath().set("windows/x86-64");
        addWindowsArgs(windowsx86_64);

        windowsx86.getPlatformPath().set("windows/x86");
        addWindowsArgs(windowsx86);

        linuxx86_64.getPlatformPath().set("linux/x86-64");
        addLinuxArgs(linuxx86_64);

        osxx86_64.getPlatformPath().set("osx/x86-64");
        addMacArgs(osxx86_64);
    }

    public static abstract class DependencyVersions {
        public abstract Property<String> getWpiVersion();

        public abstract Property<String> getNiLibVersion();

        public abstract Property<String> getOpencvVersion();

        public abstract Property<String> getGoogleTestVersion();

        public abstract Property<String> getImguiVersion();

        public abstract Property<String> getWpimathVersion();
    }

    private void addPlatformReleaseSymbolGeneration(String platform) {
        PlatformConfig plat = windowsPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getReleaseArgs().add(defaultArguments.windowsSymbolArg);
            plat.getcCompiler().getReleaseArgs().add(defaultArguments.windowsSymbolArg);
            return;
        }
        plat = unixPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getArgs().add(defaultArguments.unixSymbolArg);
            plat.getcCompiler().getArgs().add(defaultArguments.unixSymbolArg);
            if (platform.equals(platforms.osxx64)) {
                plat.getObjcCompiler().getArgs().add(defaultArguments.unixSymbolArg);
                plat.getObjcppCompiler().getArgs().add(defaultArguments.unixSymbolArg);
            }
            return;
        }
    }

    public void addReleaseSymbolGeneration(String... platforms) {
        if (platforms.length == 0) {
            for (String platform : this.platforms.allPlatforms) {
                addPlatformReleaseSymbolGeneration(platform);
            }
        } else {
            for (String platform : platforms) {
                addPlatformReleaseSymbolGeneration(platform);
            }
        }
    }

    private void addPlatformWarnings(String platform) {
        PlatformConfig plat = windowsPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getArgs().addAll(0, defaultArguments.windowsWarningArgs);
            plat.getcCompiler().getArgs().addAll(0, defaultArguments.windowsWarningArgs);
            return;
        }
        plat = unixPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getArgs().addAll(0, defaultArguments.unixWarningArgs);
            plat.getcCompiler().getArgs().addAll(0, defaultArguments.unixWarningArgs);
            if (platform.equals(platforms.osxx64)) {
                plat.getObjcCompiler().getArgs().addAll(0, defaultArguments.unixWarningArgs);
                plat.getObjcppCompiler().getArgs().addAll(0, defaultArguments.unixWarningArgs);
            }
            return;
        }
    }

    public void addWarnings(String... platforms) {
        if (platforms.length == 0) {
            for (String platform : this.platforms.allPlatforms) {
                addPlatformWarnings(platform);
            }
        } else {
            for (String platform : platforms) {
                addPlatformWarnings(platform);
            }
        }
    }

    private void addPlatformWarningsAsErrors(String platform) {
        PlatformConfig plat = windowsPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getArgs().addAll(0, defaultArguments.windowsWarningsAsErrorsArgs);
            plat.getcCompiler().getArgs().addAll(0, defaultArguments.windowsWarningsAsErrorsArgs);
            return;
        }
        plat = unixPlatforms.get(platform);
        if (plat != null) {
            plat.getCppCompiler().getArgs().addAll(0, defaultArguments.unixWarningsAsErrorsArgs);
            plat.getcCompiler().getArgs().addAll(0, defaultArguments.unixWarningsAsErrorsArgs);
            if (platform.equals(platforms.osxx64)) {
                plat.getObjcCompiler().getArgs().addAll(0, defaultArguments.unixWarningArgs);
                plat.getObjcppCompiler().getArgs().addAll(0, defaultArguments.unixWarningsAsErrorsArgs);
            }
            return;
        }
    }

    public void addWarningsAsErrors(String... platforms) {
        if (platforms.length == 0) {
            for (String platform : this.platforms.allPlatforms) {
                addPlatformWarningsAsErrors(platform);
            }
        } else {
            for (String platform : platforms) {
                addPlatformWarningsAsErrors(platform);
            }
        }
    }

    private DependencyVersions dependencyVersions;

    private void registerStandardDependency(ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs,
            String name, String groupId, String artifactId, Property<String> version) {
        configs.register(name + "_shared", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
        configs.register(name + "static", WPIStaticMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    private void registerStaticOnlyStandardDependency(ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs,
            String name, String groupId, String artifactId, Property<String> version) {
        configs.register(name + "static", WPIStaticMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    public void configureDependencies(Action<DependencyVersions> dependencies) {
        if (dependencyVersions != null) {
            return;
        }
        dependencyVersions = objects.newInstance(DependencyVersions.class);
        dependencies.execute(dependencyVersions);
        ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs = nativeExt.getNativeDependencyContainer();
        configs.register("netcomm", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set("edu.wpi.first.ni-libraries");
            c.getArtifactId().set("netcomm");
            c.getHeaderClassifier().set("headers");
            c.getExt().set("zip");
            c.getVersion().set(dependencyVersions.getNiLibVersion());
            c.getSkipAtRuntime().set(true);
            c.getTargetPlatforms().add(this.platforms.roborio);
        });

        configs.register("chipobject", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set("edu.wpi.first.ni-libraries");
            c.getArtifactId().set("chipobject");
            c.getHeaderClassifier().set("headers");
            c.getExt().set("zip");
            c.getVersion().set(dependencyVersions.getNiLibVersion());
            c.getSkipAtRuntime().set(true);
            c.getTargetPlatforms().add(this.platforms.roborio);
        });

        configs.register("visa", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set("edu.wpi.first.ni-libraries");
            c.getArtifactId().set("visa");
            c.getHeaderClassifier().set("headers");
            c.getExt().set("zip");
            c.getVersion().set(dependencyVersions.getNiLibVersion());
            c.getSkipAtRuntime().set(true);
            c.getTargetPlatforms().add(this.platforms.roborio);
        });

        configs.register("ni_runtime", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set("edu.wpi.first.ni-libraries");
            c.getArtifactId().set("runtime");
            c.getExt().set("zip");
            c.getVersion().set(dependencyVersions.getNiLibVersion());
            c.getSkipAtRuntime().set(true);
            c.getTargetPlatforms().add(this.platforms.roborio);
        });

        configs.register("ni_link_libraries", CombinedIgnoreMissingPlatformNativeDependency.class, c -> {
            c.getDependencies().put(this.platforms.roborio, List.of("netcomm", "chipobject", "visa"));
        });

        configs.register("ni_runtime_libraries", CombinedIgnoreMissingPlatformNativeDependency.class, c -> {
            c.getDependencies().put(this.platforms.roborio, List.of("ni_runtime"));
        });

        Property<String> wpiVersion = dependencyVersions.getWpiVersion();
        registerStandardDependency(configs, "wpiutil", "edu.wpi.first.wpiutil", "wpiutil-cpp", wpiVersion);
        registerStandardDependency(configs, "ntcore", "edu.wpi.first.ntcore", "ntcore-cpp", wpiVersion);
        registerStandardDependency(configs, "hal", "edu.wpi.first.hal", "hal-cpp", wpiVersion);
        registerStandardDependency(configs, "cscore", "edu.wpi.first.cscore", "cscore-cpp", wpiVersion);
        registerStandardDependency(configs, "cameraserver", "edu.wpi.first.cameraserver", "cameraserver-cpp", wpiVersion);
        registerStandardDependency(configs, "wpilibc", "edu.wpi.first.wpilibc", "wpilibc-cpp", wpiVersion);
        registerStandardDependency(configs, "wpilib_new_commands", "edu.wpi.first.wpilibNewCommands", "wpilibNewCommands-cpp", wpiVersion);
        registerStandardDependency(configs, "wpilib_old_commands", "edu.wpi.first.wpilibOldCommands", "wpilibOldCommands-cpp", wpiVersion);

        registerStandardDependency(configs, "wpimath", "edu.wpi.first.wpimath", "wpimath-cpp", dependencyVersions.getWpimathVersion());

        registerStandardDependency(configs, "opencv", "edu.wpi.first.thirdparty.frc2021.opencv", "opencv-cpp", dependencyVersions.getOpencvVersion());
        registerStaticOnlyStandardDependency(configs, "googletest", "edu.wpi.first.thirdparty.frc2021", "googletest", dependencyVersions.getGoogleTestVersion());
        registerStaticOnlyStandardDependency(configs, "imgui", "edu.wpi.first.thirdparty.frc2021", "imgui", dependencyVersions.getImguiVersion());


        configs.register("wpilib_jni", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("ntcore_shared", "hal_shared", "wpiutil_shared", "wpimath_shared", "ni_link_libraries"));
        });

        configs.register("wpilib_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilibc_static", "ntcore_static", "hal_static", "wpiutil_static", "wpimath_static", "ni_link_libraries"));
        });

        configs.register("wpilib_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilibc_shared", "ntcore_shared", "hal_shared", "wpiutil_shared", "wpimath_shared", "ni_link_libraries"));
        });

        configs.register("driver_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("hal_static", "wpiutil_static", "wpimath_static", "ni_link_libraries"));
        });

        configs.register("driver_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("hal_shared", "wpiutil_shared", "wpimath_shared", "ni_link_libraries"));
        });

        configs.register("wpilib_executable_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilib_shared", "ni_link_libraries", "ni_runtime_libraries"));
        });

        configs.register("wpilib_executable_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilib_static", "ni_link_libraries", "ni_runtime_libraries"));
        });

        configs.register("vision_jni_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cscore_shared", "opencv_shared"));
        });

        configs.register("vision_jni_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cscore_static", "opencv_static"));
        });

        configs.register("vision_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cameraserver_shared", "cscore_shared", "opencv_shared"));
        });

        configs.register("vision_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cameraserver_static", "cscore_static", "opencv_static"));
        });
    }
}
