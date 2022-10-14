package edu.wpi.first.toolchain;

import org.gradle.api.Project;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.ExecResult;

public class NativePlatforms {
    public static final String desktop = desktopOS() + desktopArch();
    public static final String roborio = "linuxathena";
    public static final String linuxarm32 = "linuxarm32";
    public static final String linuxarm64 = "linuxarm64";

    public static String desktopArch() {
        String arch = System.getProperty("os.arch");
        if (arch.equals("arm64") || arch.equals("aarch64")) {
            return "arm64";
        }
        if (arch.equals("arm32") || arch.equals("arm")) {
            return "arm32";
        }
        return (arch.equals("amd64") || arch.equals("x86_64")) ? "x86-64" : "x86";
    }

    public static String desktopPlatformArch(Project project) {
        if (OperatingSystem.current().isWindows()) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            return arch != null && arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "x86-64" : "x86";
        } else if (OperatingSystem.current().isMacOsX()) {
            String desktop = desktopArch();
            if (desktop.equals("x86-64")) {
                ExecResult res = project.exec(spec -> {
                    spec.commandLine("sysctl", "-in", "systtl.proc_translated");
                });
                return res.getExitValue() != 0 ? "arm64" : desktop;
            } else {
                return desktop;
            }
        } else {
            return desktopArch();
        }
    }

    public static String desktopOS() {
        return OperatingSystem.current().isWindows() ? "windows" : OperatingSystem.current().isMacOsX() ? "osx" : "linux";
    }

    public static class PlatformArchPair {
        public String platformName;
        public String arch;

        public PlatformArchPair(String platformName, String arch) {
            this.platformName = platformName;
            this.arch = arch;
        }
    }

    public static PlatformArchPair[] desktopExtraPlatforms() {
        if (OperatingSystem.current().isMacOsX()) {
            String currentArch = desktopArch();
            if (currentArch.equals("x86-64")) {
                return new PlatformArchPair[] {new PlatformArchPair("osxarm64", "arm64")};
            } else {
                return new PlatformArchPair[] {new PlatformArchPair("osxx86-64", "x86-64")};
            }
        } else {
            return new PlatformArchPair[0];
        }
    }
}
