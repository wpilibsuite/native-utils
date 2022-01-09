package edu.wpi.first.toolchain;

import org.gradle.api.logging.Logging;
import org.gradle.internal.os.OperatingSystem;

public class NativePlatforms {
    public static final String desktop = desktopOS() + desktopArch();
    public static final String roborio = "linuxathena";
    public static final String raspbian = "linuxraspbian";
    public static final String aarch64bionic = "linuxaarch64bionic";

    public static String desktopArch() {
        String arch = System.getProperty("os.arch");
        // Treat aarch64 or arm64 as x86-64
        // This will make it so Java programs using aarch64 JDKs will build,
        // even if they won't correctly run.
        if (arch.equals("arm64") || arch.equals("aarch64")) {
            Logging.getLogger(NativePlatforms.class).warn("Arm64 JDK's are not supported. Simulation will not work");
            return "x86-64";
        }
        return (arch.equals("amd64") || arch.equals("x86_64")) ? "x86-64" : "x86";
    }

    public static String desktopPlatformArch() {
        if (OperatingSystem.current().isWindows()) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "x86-64" : "x86";
        } else {
            return desktopArch();
        }
    }

    public static String desktopOS() {
        return OperatingSystem.current().isWindows() ? "windows" : OperatingSystem.current().isMacOsX() ? "osx" : "linux";
    }
}
