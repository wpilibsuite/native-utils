package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Action;

import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class WPINativeUtilsExtension {
    private NativeUtilsExtension nativeExt;

    public class DefaultArguments {

        public List<String> windowsCompilerArgs = Collections.unmodifiableList(
                Arrays.asList("/EHsc", "/FS", "/Zc:inline", "/wd4244", "/wd4267", "/wd4146", "/wd4996",
                        "/Zc:throwingNew", "/D_CRT_SECURE_NO_WARNINGS", "/std:cpp17", "/std:c++17", "/permissive-", "/bigobj"));
        public List<String> windowsCCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("/FS", "/Zc:inline", "/D_CRT_SECURE_NO_WARNINGS"));
        public List<String> windowsReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("/O2", "/MD"));
        public List<String> windowsDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("/Od", "/MDd"));
        public List<String> windowsLinkerArgs = Collections.unmodifiableList(Arrays.asList("/DEBUG:FULL", "/PDBALTPATH:%_PDB%"));
        public List<String> windowsReleaseLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("/OPT:REF", "/OPT:ICF"));

        public String windowsSymbolArg = "/Zi";

        public List<String> windowsWarningArgs = Collections.unmodifiableList(Arrays.asList("/W3"));
        public List<String> windowsWarningsAsErrorsArgs = Collections.unmodifiableList(Arrays.asList("/WX"));

        public List<String> unixWarningArgs = Collections.unmodifiableList(Arrays.asList("-Wall", "-Wextra"));
        public List<String> unixWarningsAsErrorsArgs = Collections.unmodifiableList(Arrays.asList("-Werror"));

        public String unixSymbolArg = "-g";

        public List<String> linuxCrossCompilerArgs = Collections.unmodifiableList(
                Arrays.asList("-std=c++17", "-Wformat=2", "-pedantic", "-Wno-psabi", "-Wno-unused-parameter",
                        "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCrossCCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Wformat=2",
                "-pedantic", "-Wno-psabi", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCrossLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl", "-latomic"));
        public List<String> linuxCrossReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
        public List<String> linuxCrossDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Og"));

        public List<String> linuxCompilerArgs = Collections.unmodifiableList(
                Arrays.asList("-std=c++17", "-Wformat=2", "-pedantic", "-Wno-psabi", "-Wno-unused-parameter",
                        "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Wformat=2", "-pedantic",
                "-Wno-psabi", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl", "-latomic"));
        public List<String> linuxReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
        public List<String> linuxDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O0"));

        public List<String> macCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("-std=c++17", "-pedantic", "-fPIC", "-Wno-unused-parameter",
                        "-Wno-error=deprecated-declarations", "-Wno-missing-field-initializers",
                        "-Wno-unused-private-field", "-Wno-unused-const-variable", "-pthread"));
        public List<String> macCCompilerArgs = Collections.unmodifiableList(Arrays.asList("-pedantic", "-fPIC",
                "-Wno-unused-parameter", "-Wno-missing-field-initializers", "-Wno-unused-private-field"));
        public List<String> macObjCppCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("-std=c++17", "-stdlib=libc++", "-fobjc-arc", "-fPIC"));
        public List<String> macReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
        public List<String> macDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O0"));
        public List<String> macLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("-framework", "CoreFoundation", "-framework", "AVFoundation",
                        "-framework", "Foundation", "-framework", "CoreMedia", "-framework", "CoreVideo"));
    }

    public class Platforms {
        public String roborio = "linuxathena";
        public String raspbian = "linuxraspbian";
        public String windowsx64 = "windowsx86-64";
        public String windowsx86 = "windowsx86";
        public String osxx64 = "osxx86-64";
        public String linuxx64 = "linuxx86-64";
        public String aarch64bionic = "linuxaarch64bionic";
        public String aarch64xenial = "linuxaarch64xenial";
        public List<String> allPlatforms = Collections.unmodifiableList(Arrays.asList(roborio, raspbian, aarch64bionic,
                aarch64xenial, windowsx64, windowsx86, osxx64, linuxx64));
        public List<String> desktopPlatforms = Collections
                .unmodifiableList(Arrays.asList(windowsx64, windowsx86, osxx64, linuxx64));
    }

    public Platforms platforms = new Platforms();
    public DefaultArguments defaultArguments = new DefaultArguments();

    private Map<String, PlatformConfig> windowsPlatforms = new HashMap<>();
    private Map<String, PlatformConfig> unixPlatforms = new HashMap<>();

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
        platform.getObjcppCompiler().getArgs().addAll(defaultArguments.macObjCppCompilerArgs);

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

    @Inject
    public WPINativeUtilsExtension(NativeUtilsExtension nativeExt) {
        this.nativeExt = nativeExt;

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

        linuxathena.setPlatformPath("linux/athena");
        addLinuxCrossArgs(linuxathena);

        linuxraspbian.setPlatformPath("linux/raspbian");
        addLinuxCrossArgs(linuxraspbian);

        linuxbionic.setPlatformPath("linux/aarch64bionic");
        addLinuxCrossArgs(linuxbionic);

        linuxxenial.setPlatformPath("linux/aarch64xenial");
        addLinuxCrossArgs(linuxxenial);

        windowsx86_64.setPlatformPath("windows/x86-64");
        addWindowsArgs(windowsx86_64);

        windowsx86.setPlatformPath("windows/x86");
        addWindowsArgs(windowsx86);

        linuxx86_64.setPlatformPath("linux/x86-64");
        addLinuxArgs(linuxx86_64);

        osxx86_64.setPlatformPath("osx/x86-64");
        addMacArgs(osxx86_64);
    }

    public class DependencyVersions {
        public String wpiVersion = "-1";
        public String niLibVersion = "-1";
        public String opencvVersion = "-1";
        public String googleTestVersion = "-1";
        public String imguiVersion = "-1";
        public String wpimathVersion = "-1";
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

    private boolean skipRaspbianAsDesktop = false;

    public void setSkipRaspbianAsDesktop(boolean skip) {
        skipRaspbianAsDesktop = skip;
    }

    public void configureDependencies(Action<DependencyVersions> dependencies) {
        if (dependencyVersions != null) {
            return;
        }
        dependencyVersions = new DependencyVersions();
        dependencies.execute(dependencyVersions);
        nativeExt.dependencyConfigs(configs -> {
            configs.create("netcomm", c -> {
                c.setGroupId("edu.wpi.first.ni-libraries");
                c.setArtifactId("netcomm");
                c.setHeaderClassifier("headers");
                c.setExt("zip");
                c.setVersion(dependencyVersions.niLibVersion);
                c.setSharedUsedAtRuntime(false);
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            configs.create("chipobject", c -> {
                c.setGroupId("edu.wpi.first.ni-libraries");
                c.setArtifactId("chipobject");
                c.setHeaderClassifier("headers");
                c.setExt("zip");
                c.setVersion(dependencyVersions.niLibVersion);
                c.setSharedUsedAtRuntime(false);
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            configs.create("visa", c -> {
                c.setGroupId("edu.wpi.first.ni-libraries");
                c.setArtifactId("visa");
                c.setHeaderClassifier("headers");
                c.setExt("zip");
                c.setVersion(dependencyVersions.niLibVersion);
                c.setSharedUsedAtRuntime(false);
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            configs.create("ni_runtime", c -> {
                c.setGroupId("edu.wpi.first.ni-libraries");
                c.setArtifactId("runtime");
                c.setExt("zip");
                c.setVersion(dependencyVersions.niLibVersion);
                c.setSharedUsedAtRuntime(false);
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            if (!dependencyVersions.wpiVersion.equals("-1")) {

                configs.create("wpiutil", c -> {
                    c.setGroupId("edu.wpi.first.wpiutil");
                    c.setArtifactId("wpiutil-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedExcludes().add("**/*jni*");
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("ntcore", c -> {
                    c.setGroupId("edu.wpi.first.ntcore");
                    c.setArtifactId("ntcore-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.getSharedExcludes().add("**/*jni*");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("hal", c -> {
                    c.setGroupId("edu.wpi.first.hal");
                    c.setArtifactId("hal-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.getSharedExcludes().add("**/*jni*");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("cscore", c -> {
                    c.setGroupId("edu.wpi.first.cscore");
                    c.setArtifactId("cscore-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.getSharedExcludes().add("**/*jni*");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("cameraserver", c -> {
                    c.setGroupId("edu.wpi.first.cameraserver");
                    c.setArtifactId("cameraserver-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.getSharedExcludes().add("**/*jni*");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("wpilibc", c -> {
                    c.setGroupId("edu.wpi.first.wpilibc");
                    c.setArtifactId("wpilibc-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.getSharedExcludes().add("**/*jni*");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("wpilib_new_commands", c -> {
                    c.setGroupId("edu.wpi.first.wpilibNewCommands");
                    c.setArtifactId("wpilibNewCommands-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });

                configs.create("wpilib_old_commands", c -> {
                    c.setGroupId("edu.wpi.first.wpilibOldCommands");
                    c.setArtifactId("wpilibOldCommands-cpp");
                    c.setHeaderClassifier("headers");
                    c.setSourceClassifier("sources");
                    c.setExt("zip");
                    c.setVersion(dependencyVersions.wpiVersion);
                    c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                    c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                });
            }

            configs.create("opencv", c -> {
                c.setGroupId("edu.wpi.first.thirdparty.frc2020.opencv");
                c.setArtifactId("opencv-cpp");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.opencvVersion);
                c.getSharedExcludes().add("**/*java*");
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });

            configs.create("googletest", c -> {
                c.setGroupId("edu.wpi.first.thirdparty.frc2020");
                c.setArtifactId("googletest");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.googleTestVersion);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });

            configs.create("imgui", c -> {
                c.setGroupId("edu.wpi.first.thirdparty.frc2020");
                c.setArtifactId("imgui");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.imguiVersion);
                c.getStaticPlatforms().addAll(this.platforms.desktopPlatforms);
                c.getSharedPlatforms().addAll(this.platforms.desktopPlatforms);
            });

            configs.create("wpimath", c -> {
                c.setGroupId("edu.wpi.first.math");
                c.setArtifactId("wpimath-cpp");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.wpimathVersion);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
            });
        });
        if (!dependencyVersions.wpiVersion.equals("-1")) {
            nativeExt.combinedDependencyConfigs(configs -> {
                configs.create("wpilib_jni_rio", c -> {
                    c.setLibraryName("wpilib_jni");
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    List<String> deps = c.getDependencies();
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                });

                configs.create("wpilib_static_rio", c -> {
                    c.setLibraryName("wpilib_static");
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    List<String> deps = c.getDependencies();
                    deps.add("wpilibc_static");
                    deps.add("ntcore_static");
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                    deps.add("wpimath_static");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                });
                configs.create("wpilib_executable_static_rio", c -> {
                    c.setLibraryName("wpilib_executable_static");
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    List<String> deps = c.getDependencies();
                    deps.add("wpilibc_static");
                    deps.add("ntcore_static");
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                    deps.add("wpimath_static");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                    deps.add("ni_runtime_shared");
                });
                configs.create("driver_static_rio", c -> {
                    c.setLibraryName("driver_static");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                });
                configs.create("wpilib_shared_rio", c -> {
                    c.setLibraryName("wpilib_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    deps.add("wpilibc_shared");
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                });
                configs.create("wpilib_executable_shared_rio", c -> {
                    c.setLibraryName("wpilib_executable_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    deps.add("wpilibc_shared");
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                    deps.add("ni_runtime_shared");
                });
                configs.create("driver_shared_rio", c -> {
                    c.setLibraryName("driver_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().add(this.platforms.roborio);
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("chipobject_shared");
                    deps.add("netcomm_shared");
                    deps.add("visa_shared");
                });

                configs.create("vision_jni_shared", c -> {
                    c.setLibraryName("vision_jni_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
                    deps.add("cscore_shared");
                    deps.add("opencv_shared");
                });

                configs.create("vision_shared", c -> {
                    c.setLibraryName("vision_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
                    deps.add("cameraserver_shared");
                    deps.add("cscore_shared");
                    deps.add("opencv_shared");
                });

                configs.create("vision_jni_static", c -> {
                    c.setLibraryName("vision_jni_static");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
                    deps.add("cscore_static");
                    deps.add("opencv_static");
                });

                configs.create("vision_static", c -> {
                    c.setLibraryName("vision_static");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(this.platforms.allPlatforms);
                    deps.add("cameraserver_static");
                    deps.add("cscore_static");
                    deps.add("opencv_static");
                });

                List<String> platsWithoutRio = new ArrayList<>(this.platforms.allPlatforms);
                platsWithoutRio.remove(this.platforms.roborio);

                if (skipRaspbianAsDesktop) {
                    platsWithoutRio.remove(this.platforms.raspbian);
                }

                configs.create("wpilib_jni_dt", c -> {
                    c.setLibraryName("wpilib_jni");
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    List<String> deps = c.getDependencies();
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                });

                configs.create("wpilib_static_dt", c -> {
                    c.setLibraryName("wpilib_static");
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    List<String> deps = c.getDependencies();
                    deps.add("wpilibc_static");
                    deps.add("ntcore_static");
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                    deps.add("wpimath_static");
                });
                configs.create("wpilib_executable_static_dt", c -> {
                    c.setLibraryName("wpilib_executable_static");
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    List<String> deps = c.getDependencies();
                    deps.add("wpilibc_static");
                    deps.add("ntcore_static");
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                    deps.add("wpimath_static");
                });
                configs.create("driver_static_dt", c -> {
                    c.setLibraryName("driver_static");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    deps.add("hal_static");
                    deps.add("wpiutil_static");
                });
                configs.create("wpilib_shared_dt", c -> {
                    c.setLibraryName("wpilib_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    deps.add("wpilibc_shared");
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                });
                configs.create("wpilib_executable_shared_dt", c -> {
                    c.setLibraryName("wpilib_executable_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    deps.add("wpilibc_shared");
                    deps.add("ntcore_shared");
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                    deps.add("wpimath_shared");
                });
                configs.create("driver_shared_dt", c -> {
                    c.setLibraryName("driver_shared");
                    List<String> deps = c.getDependencies();
                    c.getTargetPlatforms().addAll(platsWithoutRio);
                    deps.add("hal_shared");
                    deps.add("wpiutil_shared");
                });
            });
        }
    }
}
