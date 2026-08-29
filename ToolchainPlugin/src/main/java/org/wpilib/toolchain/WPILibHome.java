package org.wpilib.toolchain;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public class WPILibHome {

    private String year;
    private File wpilibFolder;

    public WPILibHome(String year) {
        this.year = year;
        this.wpilibFolder = new File(computeHomeRoot(), year);
    }

    private static File computeHomeRoot() {
        OperatingSystem currentOperatingSystem = OperatingSystem.current();
        return computeHomeRoot(currentOperatingSystem.isWindows(), currentOperatingSystem.isMacOsX(),
                currentOperatingSystem.isLinux(), System.getProperty("user.home"), System.getenv("PUBLIC"),
                System.getenv("XDG_DATA_HOME"));
    }

    static File computeHomeRoot(boolean isWindows, boolean isMacOsX, boolean isLinux, String userFolder,
            String publicFolder, String xdgDataHome) {
        if (isWindows) {
            String resolvedPublicFolder = publicFolder;
            if (resolvedPublicFolder == null) {
                resolvedPublicFolder = "C:\\Users\\Public";
            }
            return new File(resolvedPublicFolder, "wpilib");
        }

        if (isMacOsX) {
            return new File(userFolder, ".wpilib");
        }

        if (isLinux && xdgDataHome != null && !xdgDataHome.isBlank()
                && (new File(xdgDataHome).isAbsolute() || xdgDataHome.startsWith("/"))) {
            return new File(xdgDataHome, "wpilib");
        }

        if (isLinux) {
            return new File(new File(userFolder, ".local/share"), "wpilib");
        }

        return new File(userFolder, ".wpilib");
    }

    public File get() {
        return wpilibFolder;
    }

    public String year() {
        return year;
    }
}
