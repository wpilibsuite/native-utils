package edu.wpi.first.toolchain;

import org.gradle.internal.os.OperatingSystem;

public class NativePlatforms {
    public static final String desktop = desktopOS() + desktopArch();
    public static final String roborio = "linuxathena";
    public static final String raspbian = "linuxraspbian";
    public static final String aarch64bionic = "linuxaarch64bionic";
    public static final String aarch64xenial = "linuxaarch64xenial";

    public static String desktopArch() {
        String arch = System.getProperty("os.arch");
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
