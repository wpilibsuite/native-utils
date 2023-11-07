package edu.wpi.first.nativeutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import edu.wpi.first.nativeutils.platforms.PlatformConfig;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsPlugin;
import edu.wpi.first.toolchain.NativePlatforms;
import edu.wpi.first.nativeutils.dependencies.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.CombinedIgnoreMissingPlatformNativeDependency;
import edu.wpi.first.nativeutils.dependencies.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.WPISharedMavenDependency;
import edu.wpi.first.nativeutils.dependencies.WPIStaticMavenDependency;

public class WPINativeUtilsExtension {
    private NativeUtilsExtension nativeExt;

    private final NativePlatforms nativePlatforms = new NativePlatforms();

    public NativePlatforms getNativePlatforms() {
        return nativePlatforms;
    }

    public static class DefaultArguments {

        public final List<String> windowsCompilerArgs = List.of("/EHsc", "/FS", "/Zc:inline", "/wd4244", "/wd4267",
                "/wd4146", "/wd4996", "/Zc:throwingNew", "/D_CRT_SECURE_NO_WARNINGS", "/std:c++20", "/permissive-",
                "/utf-8", "/bigobj", "/Zc:__cplusplus", "/Zc:preprocessor", "/wd5105"); // 5105 is thrown by windows sdk headers
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

        public final String unixRpathOriginArg = "-Wl,-rpath,'$ORIGIN'";
        public final String unixSymbolArg = "-g";

        // -Wdeprecated-enum-enum-conversion was introduced in GCC 11
        public final List<String> linuxCrossCompilerArgs = List.of("-std=c++20", "-Wformat=2", "-pedantic",
                "-Wno-psabi", "-Wno-unused-parameter", "-fPIC", "-pthread");
        public final List<String> linuxCrossCompilerExtraArgs11 = List.of("-Wno-error=deprecated-enum-enum-conversion");
        public final List<String> linuxCrossCompilerExtraArgs10 = List.of("-Wno-error=deprecated-declarations");
        public final List<String> linuxCrossCCompilerArgs = List.of("-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-fPIC", "-pthread");
        public final List<String> linuxCrossLinkerArgs = List.of("-rdynamic", "-pthread", "-ldl", "-latomic");
        public final List<String> linuxCrossReleaseCompilerArgs = List.of("-O2");
        public final List<String> linuxCrossDebugCompilerArgs = List.of("-Og");

        public final List<String> linuxCompilerArgs = List.of("-std=c++20", "-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-Wno-error=deprecated-enum-enum-conversion", "-fPIC", "-pthread");
        public final List<String> linuxCCompilerArgs = List.of("-Wformat=2", "-pedantic", "-Wno-psabi",
                "-Wno-unused-parameter", "-fPIC", "-pthread");
        public final List<String> linuxLinkerArgs = List.of("-rdynamic", "-pthread", "-ldl", "-latomic");
        public final List<String> linuxReleaseCompilerArgs = List.of("-O2");
        public final List<String> linuxDebugCompilerArgs = List.of("-O0");

        public final String macMinimumVersionArg = "-mmacosx-version-min=12";

        public final List<String> macCompilerArgs = List.of("-std=c++20", "-pedantic", "-fPIC", "-Wno-unused-parameter",
                "-Wno-error=deprecated-enum-enum-conversion", "-Wno-missing-field-initializers",
                "-Wno-unused-private-field", "-Wno-unused-const-variable", "-Wno-error=c11-extensions", "-pthread");
        public final List<String> macCCompilerArgs = List.of("-pedantic", "-fPIC", "-Wno-unused-parameter",
                "-Wno-missing-field-initializers", "-Wno-unused-private-field", "-Wno-fixed-enum-extension");
        public final List<String> macObjcppCompilerArgs = List.of("-std=c++20", "-stdlib=libc++", "-fobjc-weak",
                "-fobjc-arc", "-fPIC");
        public final List<String> macObjcCompilerArgs = List.of("-fobjc-weak", "-fobjc-arc", "-fPIC");
        public final List<String> macReleaseCompilerArgs = List.of("-O2");
        public final List<String> macDebugCompilerArgs = List.of("-O0");
        public final List<String> macLinkerArgs = List.of("-framework", "CoreFoundation", "-framework", "AVFoundation",
                "-framework", "Foundation", "-framework", "CoreMedia", "-framework", "CoreVideo",
                "-headerpad_max_install_names");
    }

    public static class Platforms {
        public final String roborio = "linuxathena";
        public final String linuxarm32 = "linuxarm32";
        public final String linuxarm64 = "linuxarm64";
        public final String windowsx64 = "windowsx86-64";
        public final String windowsx86 = "windowsx86";
        public final String windowsarm64 = "windowsarm64";
        public final String osxuniversal = "osxuniversal";
        public final String linuxx64 = "linuxx86-64";
        public final List<String> allPlatforms = List.of(roborio, linuxarm32, linuxarm64, windowsx64,
                windowsx86, windowsarm64, osxuniversal, linuxx64);
        public final List<String> desktopPlatforms = List.of(windowsx64, windowsx86, windowsarm64, osxuniversal, linuxx64);
    }

    public final Platforms platforms;

    public final DefaultArguments defaultArguments;

    private final Map<String, PlatformConfig> windowsPlatforms = new HashMap<>();
    private final Map<String, PlatformConfig> unixPlatforms = new HashMap<>();

    public void addLinuxCrossArgs(PlatformConfig platform, int gccMajor) {
        platform.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerArgs);
        if (gccMajor >= 11) {
            platform.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerExtraArgs11);
        } else {
            platform.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerExtraArgs10);
        }
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
    private final ProviderFactory provider;

    public void addVendorDeps() {
        project.getPlugins().apply(WPIVendorDepsPlugin.class);
    }

    private WPIVendorDepsExtension vendorDeps;

    public WPIVendorDepsExtension getVendorDeps() {
        if (vendorDeps == null) {
            vendorDeps = project.getExtensions().getByType(WPIVendorDepsExtension.class);
        }
        return vendorDeps;
    }

    private final Project project;

    @Inject
    public WPINativeUtilsExtension(NativeUtilsExtension nativeExt, Project project) {
        this.nativeExt = nativeExt;
        this.objects = project.getObjects();
        this.provider = project.getProviders();
        this.project = project;

        this.platforms = objects.newInstance(Platforms.class);
        defaultArguments = objects.newInstance(DefaultArguments.class);

        PlatformConfig windowsx86_64 = nativeExt.getPlatformConfigs().create(platforms.windowsx64);
        PlatformConfig windowsx86 = nativeExt.getPlatformConfigs().create(platforms.windowsx86);
        PlatformConfig windowsarm64 = nativeExt.getPlatformConfigs().create(platforms.windowsarm64);
        windowsPlatforms.put(platforms.windowsx64, windowsx86_64);
        windowsPlatforms.put(platforms.windowsx86, windowsx86);
        windowsPlatforms.put(platforms.windowsarm64, windowsarm64);
        PlatformConfig linuxx86_64 = nativeExt.getPlatformConfigs().create(platforms.linuxx64);
        PlatformConfig osxuniversal = nativeExt.getPlatformConfigs().create(platforms.osxuniversal);
        PlatformConfig linuxathena = nativeExt.getPlatformConfigs().create(platforms.roborio);
        PlatformConfig linuxarm32 = nativeExt.getPlatformConfigs().create(platforms.linuxarm32);
        PlatformConfig linuxarm64 = nativeExt.getPlatformConfigs().create(platforms.linuxarm64);
        unixPlatforms.put(platforms.linuxx64, linuxx86_64);
        unixPlatforms.put(platforms.osxuniversal, osxuniversal);
        unixPlatforms.put(platforms.linuxarm32, linuxarm32);
        unixPlatforms.put(platforms.roborio, linuxathena);
        unixPlatforms.put(platforms.linuxarm64, linuxarm64);

        linuxathena.getPlatformPath().set("linux/athena");
        addLinuxCrossArgs(linuxathena, 12);

        linuxarm32.getPlatformPath().set("linux/arm32");
        addLinuxCrossArgs(linuxarm32, 10);

        linuxarm64.getPlatformPath().set("linux/arm64");
        addLinuxCrossArgs(linuxarm64, 10);

        windowsx86.getPlatformPath().set("windows/x86");
        addWindowsArgs(windowsx86);

        windowsx86_64.getPlatformPath().set("windows/x86-64");
        addWindowsArgs(windowsx86_64);

        windowsarm64.getPlatformPath().set("windows/arm64");
        addWindowsArgs(windowsarm64);

        linuxx86_64.getPlatformPath().set("linux/x86-64");
        addLinuxArgs(linuxx86_64);

        osxuniversal.getPlatformPath().set("osx/universal");
        addMacArgs(osxuniversal);
    }

    public void addGcc11CrossArgs(String platform) {
        PlatformConfig config = unixPlatforms.get(platform);
        if (config != null) {
            config.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerExtraArgs11);
        }
    }

    public void addMacMinimumVersionArg() {
        PlatformConfig platform = unixPlatforms.get(platforms.osxuniversal);
        platform.getcCompiler().getArgs().add(defaultArguments.macMinimumVersionArg);
        platform.getCppCompiler().getArgs().add(defaultArguments.macMinimumVersionArg);
        platform.getObjcCompiler().getArgs().add(defaultArguments.macMinimumVersionArg);
        platform.getObjcppCompiler().getArgs().add(defaultArguments.macMinimumVersionArg);
    }

    public static abstract class DependencyVersions {
        public abstract Property<String> getWpiVersion();

        public abstract Property<String> getNiLibVersion();

        public abstract Property<String> getOpencvVersion();

        public abstract Property<String> getGoogleTestVersion();

        public abstract Property<String> getOpencvYear();

        public abstract Property<String> getGoogleTestYear();

        public abstract Property<String> getWpimathVersion();

        public abstract Property<String> getImguiYear();

        public abstract Property<String> getImguiVersion();
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
            if (platform.equals(platforms.osxuniversal)) {
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
            if (platform.equals(platforms.osxuniversal)) {
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
            if (platform.equals(platforms.osxuniversal)) {
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

    private void addPlatformRpathAsOrigin(String platform) {
        PlatformConfig plat = unixPlatforms.get(platform);
        if (plat != null) {
            plat.getLinker().getArgs().add(defaultArguments.unixRpathOriginArg);
            return;
        }
    }

    public void addPlatformRpathAsOrigin(String... platforms) {
        if (platforms.length == 0) {
            for (String platform : this.platforms.allPlatforms) {
                addPlatformRpathAsOrigin(platform);
            }
        } else {
            for (String platform : platforms) {
                addPlatformRpathAsOrigin(platform);
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
            c.getExtraSharedExcludes().add("**/*java*");
            c.getExtraSharedExcludes().add("**/*jni*");
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
        configs.register(name + "_static", WPIStaticMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getExtraSharedExcludes().add("**/*java*");
            c.getExtraSharedExcludes().add("**/*jni*");
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    private void registerStandardDependency(ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs,
            String name, Provider<String> groupId, String artifactId, Property<String> version) {
        configs.register(name + "_shared", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getExtraSharedExcludes().add("**/*java*");
            c.getExtraSharedExcludes().add("**/*jni*");
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
        configs.register(name + "_static", WPIStaticMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getExtraSharedExcludes().add("**/*java*");
            c.getExtraSharedExcludes().add("**/*jni*");
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    private void registerSharedOnlyStandardDependency(
            ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs,
            String name, String groupId, String artifactId, Property<String> version) {
        configs.register(name + "_shared", WPISharedMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getExtraSharedExcludes().add("**/*java*");
            c.getExtraSharedExcludes().add("**/*jni*");
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    private void registerStaticOnlyStandardDependency(
            ExtensiblePolymorphicDomainObjectContainer<NativeDependency> configs,
            String name, Provider<String> groupId, String artifactId, Property<String> version) {
        configs.register(name + "_static", WPIStaticMavenDependency.class, c -> {
            c.getGroupId().set(groupId);
            c.getArtifactId().set(artifactId);
            c.getHeaderClassifier().set("headers");
            c.getSourceClassifier().set("sources");
            c.getExt().set("zip");
            c.getVersion().set(version);
            c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
        });
    }

    private DependencyVersions versions;

    public DependencyVersions getVersions() {
        return versions;
    }

    public void configureDependencies(Action<DependencyVersions> dependencies) {
        if (dependencyVersions != null) {
            return;
        }
        dependencyVersions = objects.newInstance(DependencyVersions.class);
        dependencyVersions.getGoogleTestYear().set("Unknown");
        dependencyVersions.getOpencvYear().set("Unknown");

        dependencyVersions.getWpiVersion().set("-1");
        dependencyVersions.getNiLibVersion().set("-1");
        dependencyVersions.getOpencvVersion().set("-1");
        dependencyVersions.getGoogleTestVersion().set("-1");

        dependencyVersions.getWpimathVersion().set("-1");
        dependencyVersions.getImguiYear().set("-1");
        dependencyVersions.getImguiVersion().set("-1");

        dependencies.execute(dependencyVersions);
        versions = dependencyVersions;
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
        registerStandardDependency(configs, "wpinet", "edu.wpi.first.wpinet", "wpinet-cpp", wpiVersion);
        registerStandardDependency(configs, "ntcore", "edu.wpi.first.ntcore", "ntcore-cpp", wpiVersion);
        registerStandardDependency(configs, "hal", "edu.wpi.first.hal", "hal-cpp", wpiVersion);
        registerStandardDependency(configs, "cscore", "edu.wpi.first.cscore", "cscore-cpp", wpiVersion);
        registerStandardDependency(configs, "cameraserver", "edu.wpi.first.cameraserver", "cameraserver-cpp",
                wpiVersion);
        registerStandardDependency(configs, "wpilibc", "edu.wpi.first.wpilibc", "wpilibc-cpp", wpiVersion);

        registerStandardDependency(configs, "wpimath", "edu.wpi.first.wpimath", "wpimath-cpp",
                wpiVersion);
        registerSharedOnlyStandardDependency(configs, "apriltag", "edu.wpi.first.apriltag", "apriltag-cpp",
                wpiVersion);

        Provider<String> opencvYearGroup = provider
                .provider(() -> "edu.wpi.first.thirdparty." + dependencyVersions.getOpencvYear().get() + ".opencv");
        Provider<String> googleTestYearGroup = provider
                .provider(() -> "edu.wpi.first.thirdparty." + dependencyVersions.getGoogleTestYear().get());

        registerStandardDependency(configs, "opencv", opencvYearGroup, "opencv-cpp",
                dependencyVersions.getOpencvVersion());
        registerStaticOnlyStandardDependency(configs, "googletest", googleTestYearGroup, "googletest",
                dependencyVersions.getGoogleTestVersion());

        configs.register("wpilib_jni", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("ntcore_shared", "hal_shared", "wpimath_shared", "wpinet_shared", "wpiutil_shared",
                    "ni_link_libraries"));
        });

        configs.register("wpilib_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilibc_static", "ntcore_static", "hal_static", "wpimath_static", "wpinet_static",
                    "wpiutil_static", "ni_link_libraries"));
        });

        configs.register("wpilib_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("wpilibc_shared", "ntcore_shared", "hal_shared", "wpimath_shared", "wpinet_shared",
                    "wpiutil_shared", "ni_link_libraries"));
        });

        configs.register("driver_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("hal_static", "wpimath_static", "wpinet_static", "wpiutil_static", "ni_link_libraries"));
        });

        configs.register("driver_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("hal_shared", "wpimath_shared", "wpinet_shared", "wpiutil_shared", "ni_link_libraries"));
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
            d.set(List.of("cscore_shared", "apriltag_shared", "opencv_shared"));
        });

        configs.register("vision_jni_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cscore_static", "opencv_static"));
        });

        configs.register("vision_shared", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cameraserver_shared", "cscore_shared", "apriltag_shared", "opencv_shared"));
        });

        configs.register("vision_static", AllPlatformsCombinedNativeDependency.class, c -> {
            ListProperty<String> d = c.getDependencies();
            d.set(List.of("cameraserver_static", "cscore_static", "opencv_static"));
        });
    }
}
