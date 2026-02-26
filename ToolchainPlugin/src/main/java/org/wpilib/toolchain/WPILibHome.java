package org.wpilib.toolchain;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public class WPILibHome {

    private String year;
    private File wpilibFolder;

    public WPILibHome(String year) {
        this.year = year;

        File baseFolder;
        if (OperatingSystem.current().isWindows()) {
            String publicFolder = System.getenv("PUBLIC");
            if (publicFolder == null) {
                publicFolder = "C:\\Users\\Public";
            }
            baseFolder = new File(publicFolder, "wpilib");
        } else {
            baseFolder = new File(System.getProperty("user.home"), "wpilib");
        }
        this.wpilibFolder = new File(baseFolder, year);
    }

    public File get() {
        return wpilibFolder;
    }

    public String year() {
        return year;
    }
}
