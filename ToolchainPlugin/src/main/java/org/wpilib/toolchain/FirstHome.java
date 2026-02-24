package org.wpilib.toolchain;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public class FirstHome {

    private String year;
    private File firstFolder;

    public FirstHome(String year) {
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
        this.firstFolder = new File(baseFolder, year);
    }

    public File get() {
        return firstFolder;
    }

    public String year() {
        return year;
    }
}
