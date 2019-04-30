package edu.wpi.first.nativeutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;

import edu.wpi.first.nativeutils.configs.PlatformConfig;

public class WPINativeUtilsExtension {
    private NativeUtilsExtension nativeExt;

    public class DefaultArguments {

        public List<String> windowsCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("/EHsc", "/DNOMINMAX", "/Zi", "/FS", "/Zc:inline", "/MP4"));
        public List<String> windowsCCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("/Zi", "/FS", "/Zc:inline"));
        public List<String> windowsReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("/O2", "/MD"));
        public List<String> windowsDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("/Od", "/MDd"));
        public List<String> windowsLinkerArgs = Collections.unmodifiableList(Arrays.asList("/DEBUG:FULL"));
        public List<String> windowsReleaseLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("/OPT:REF", "/OPT:ICF"));

        public List<String> linuxCrossCompilerArgs = Collections.unmodifiableList(Arrays.asList("-std=c++14",
                "-Wformat=2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi", "-g", "-Wno-unused-parameter",
                "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCrossCCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("-Wformat=2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi",
                        "-g", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCrossLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl"));
        public List<String> linuxCrossReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
        public List<String> linuxCrossDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-Og"));

        public List<String> linuxCompilerArgs = Collections.unmodifiableList(Arrays.asList("-std=c++14", "-Wformat=2",
                "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi", "-g", "-Wno-unused-parameter",
                "-Wno-error=deprecated-declarations", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxCCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("-Wformat=2", "-Wall", "-Wextra", "-Werror", "-pedantic", "-Wno-psabi",
                        "-g", "-Wno-unused-parameter", "-fPIC", "-rdynamic", "-pthread"));
        public List<String> linuxLinkerArgs = Collections
                .unmodifiableList(Arrays.asList("-rdynamic", "-pthread", "-ldl"));
        public List<String> linuxReleaseCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O2"));
        public List<String> linuxDebugCompilerArgs = Collections.unmodifiableList(Arrays.asList("-O0"));

        public List<String> macCompilerArgs = Collections.unmodifiableList(Arrays.asList("-std=c++14", "-Wall",
                "-Wextra", "-Werror", "-pedantic-errors", "-fPIC", "-g", "-Wno-unused-parameter",
                "-Wno-error=deprecated-declarations", "-Wno-missing-field-initializers", "-Wno-unused-private-field",
                "-Wno-unused-const-variable", "-pthread"));
        public List<String> macCCompilerArgs = Collections
                .unmodifiableList(Arrays.asList("-Wall", "-Wextra", "-Werror", "-pedantic-errors", "-fPIC", "-g",
                        "-Wno-unused-parameter", "-Wno-missing-field-initializers", "-Wno-unused-private-field"));
        public List<String> macObjCppCompilerArgs = Collections.unmodifiableList(Arrays.asList("-std=c++14",
                "-stdlib=libc++", "-fobjc-arc", "-g", "-fPIC", "-Wall", "-Wextra", "-Werror"));
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
        public List<String> allPlatforms = Collections
                .unmodifiableList(Arrays.asList(roborio, raspbian, windowsx64, windowsx86, osxx64, linuxx64));
    }

    public Platforms platforms = new Platforms();
    public DefaultArguments defaultArguments = new DefaultArguments();

    @Inject
    public WPINativeUtilsExtension(NativeUtilsExtension nativeExt) {
        this.nativeExt = nativeExt;

        PlatformConfig windowsx86_64 = nativeExt.getPlatformConfigs().create(platforms.windowsx64);
        PlatformConfig windowsx86 = nativeExt.getPlatformConfigs().create(platforms.windowsx86);
        PlatformConfig linuxx86_64 = nativeExt.getPlatformConfigs().create(platforms.linuxx64);
        PlatformConfig osxx86_64 = nativeExt.getPlatformConfigs().create(platforms.osxx64);
        PlatformConfig linuxathena = nativeExt.getPlatformConfigs().create(platforms.roborio);
        PlatformConfig linuxraspbian = nativeExt.getPlatformConfigs().create(platforms.raspbian);

        linuxathena.setPlatformPath("linux/athena");
        linuxathena.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerArgs);
        linuxathena.getcCompiler().getArgs().addAll(defaultArguments.linuxCrossCCompilerArgs);
        linuxathena.getLinker().getArgs().addAll(defaultArguments.linuxCrossLinkerArgs);
        linuxathena.getCppCompiler().getDebugArgs().addAll(defaultArguments.linuxCrossDebugCompilerArgs);
        linuxathena.getCppCompiler().getReleaseArgs().addAll(defaultArguments.linuxCrossReleaseCompilerArgs);

        linuxraspbian.setPlatformPath("linux/raspbian");
        linuxraspbian.getCppCompiler().getArgs().addAll(defaultArguments.linuxCrossCompilerArgs);
        linuxraspbian.getcCompiler().getArgs().addAll(defaultArguments.linuxCrossCCompilerArgs);
        linuxraspbian.getLinker().getArgs().addAll(defaultArguments.linuxCrossLinkerArgs);
        linuxraspbian.getCppCompiler().getDebugArgs().addAll(defaultArguments.linuxCrossDebugCompilerArgs);
        linuxraspbian.getCppCompiler().getReleaseArgs().addAll(defaultArguments.linuxCrossReleaseCompilerArgs);

        windowsx86_64.setPlatformPath("windows/x86-64");
        windowsx86_64.getCppCompiler().getArgs().addAll(defaultArguments.windowsCompilerArgs);
        windowsx86_64.getcCompiler().getArgs().addAll(defaultArguments.windowsCCompilerArgs);
        windowsx86_64.getLinker().getArgs().addAll(defaultArguments.windowsLinkerArgs);
        windowsx86_64.getLinker().getReleaseArgs().addAll(defaultArguments.windowsReleaseLinkerArgs);
        windowsx86_64.getCppCompiler().getDebugArgs().addAll(defaultArguments.windowsDebugCompilerArgs);
        windowsx86_64.getCppCompiler().getReleaseArgs().addAll(defaultArguments.windowsReleaseCompilerArgs);

        windowsx86.setPlatformPath("windows/x86");
        windowsx86.getCppCompiler().getArgs().addAll(defaultArguments.windowsCompilerArgs);
        windowsx86.getcCompiler().getArgs().addAll(defaultArguments.windowsCCompilerArgs);
        windowsx86.getLinker().getArgs().addAll(defaultArguments.windowsLinkerArgs);
        windowsx86.getLinker().getReleaseArgs().addAll(defaultArguments.windowsReleaseLinkerArgs);
        windowsx86.getCppCompiler().getDebugArgs().addAll(defaultArguments.windowsDebugCompilerArgs);
        windowsx86.getCppCompiler().getReleaseArgs().addAll(defaultArguments.windowsReleaseCompilerArgs);

        linuxx86_64.setPlatformPath("linux/x86-64");
        linuxx86_64.getCppCompiler().getArgs().addAll(defaultArguments.linuxCompilerArgs);
        linuxx86_64.getcCompiler().getArgs().addAll(defaultArguments.linuxCCompilerArgs);
        linuxx86_64.getLinker().getArgs().addAll(defaultArguments.linuxLinkerArgs);
        linuxx86_64.getCppCompiler().getDebugArgs().addAll(defaultArguments.linuxDebugCompilerArgs);
        linuxx86_64.getCppCompiler().getReleaseArgs().addAll(defaultArguments.linuxReleaseCompilerArgs);

        osxx86_64.setPlatformPath("osx/x86-64");
        osxx86_64.getCppCompiler().getArgs().addAll(defaultArguments.macCompilerArgs);
        osxx86_64.getcCompiler().getArgs().addAll(defaultArguments.macCCompilerArgs);
        osxx86_64.getLinker().getArgs().addAll(defaultArguments.macLinkerArgs);
        osxx86_64.getCppCompiler().getDebugArgs().addAll(defaultArguments.macDebugCompilerArgs);
        osxx86_64.getCppCompiler().getReleaseArgs().addAll(defaultArguments.macReleaseCompilerArgs);
        osxx86_64.getObjcppCompiler().getArgs().addAll(defaultArguments.macObjCppCompilerArgs);

    }

    public class DependencyVersions {
        public String wpiVersion;
        public String niLibVersion;
        public String opencvVersion;
        public String googleTestVersion;
    }

    private DependencyVersions dependencyVersions;

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
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            configs.create("chipobject", c -> {
                c.setGroupId("edu.wpi.first.ni-libraries");
                c.setArtifactId("chipobject");
                c.setHeaderClassifier("headers");
                c.setExt("zip");
                c.setVersion(dependencyVersions.niLibVersion);
                c.getSharedPlatforms().add(this.platforms.roborio);
            });

            configs.create("wpiutil", c -> {
                c.setGroupId("edu.wpi.first.wpiutil");
                c.setArtifactId("wpiutil-cpp");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.wpiVersion);
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });

            configs.create("ntcore", c -> {
                c.setGroupId("edu.wpi.first.ntcore");
                c.setArtifactId("ntcore-cpp");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
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
                c.setVersion(dependencyVersions.wpiVersion);
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });

            configs.create("opencv", c -> {
                c.setGroupId("edu.wpi.first.thirdparty.frc2019.opencv");
                c.setArtifactId("opencv-cpp");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.opencvVersion);
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });

            configs.create("googletest", c -> {
                c.setGroupId("edu.wpi.first.thirdparty.frc2019");
                c.setArtifactId("googletest");
                c.setHeaderClassifier("headers");
                c.setSourceClassifier("sources");
                c.setExt("zip");
                c.setVersion(dependencyVersions.googleTestVersion);
                c.getSharedPlatforms().addAll(this.platforms.allPlatforms);
                c.getStaticPlatforms().addAll(this.platforms.allPlatforms);
            });
        });
        nativeExt.combinedDependencyConfigs(configs -> {
            configs.create("wpilib_static_rio", c -> {
                c.setLibraryName("wpilib_static");
                c.getTargetPlatforms().add(this.platforms.roborio);
                List<String> deps = c.getDependencies();
                deps.add("wpilibc_static");
                deps.add("cameraserver_static");
                deps.add("cscore_static");
                deps.add("opencv_static");
                deps.add("ntcore_static");
                deps.add("hal_static");
                deps.add("wpiutil_static");
                deps.add("chipobject_shared");
                deps.add("netcomm_shared");
            });
            configs.create("driver_static_rio", c -> {
                c.setLibraryName("driver_static");
                List<String> deps = c.getDependencies();
                c.getTargetPlatforms().add(this.platforms.roborio);
                deps.add("hal_static");
                deps.add("wpiutil_static");
                deps.add("chipobject_shared");
                deps.add("netcomm_shared");
            });
            configs.create("wpilib_shared_rio", c -> {
                c.setLibraryName("wpilib_shared");
                List<String> deps = c.getDependencies();
                c.getTargetPlatforms().add(this.platforms.roborio);
                deps.add("wpilibc_shared");
                deps.add("cameraserver_shared");
                deps.add("cscore_shared");
                deps.add("opencv_shared");
                deps.add("ntcore_shared");
                deps.add("hal_shared");
                deps.add("wpiutil_shared");
                deps.add("chipobject_shared");
                deps.add("netcomm_shared");
            });
            configs.create("driver_shared_rio", c -> {
                c.setLibraryName("driver_shared");
                List<String> deps = c.getDependencies();
                c.getTargetPlatforms().add(this.platforms.roborio);
                deps.add("hal_shared");
                deps.add("wpiutil_shared");
                deps.add("chipobject_shared");
                deps.add("netcomm_shared");
            });
            List<String> platsWithoutRio = new ArrayList<>(this.platforms.allPlatforms);
            platsWithoutRio.remove(this.platforms.roborio);

            configs.create("wpilib_static_dt", c -> {
                c.setLibraryName("wpilib_static");
                c.getTargetPlatforms().addAll(platsWithoutRio);
                List<String> deps = c.getDependencies();
                deps.add("wpilibc_static");
                deps.add("cameraserver_static");
                deps.add("cscore_static");
                deps.add("opencv_static");
                deps.add("ntcore_static");
                deps.add("hal_static");
                deps.add("wpiutil_static");
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
                deps.add("cameraserver_shared");
                deps.add("cscore_shared");
                deps.add("opencv_shared");
                deps.add("ntcore_shared");
                deps.add("hal_shared");
                deps.add("wpiutil_shared");
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
