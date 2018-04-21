package edu.wpi.first.nativeutils.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExportsStorage {
    static File extractedFile = null;

    public static String getGeneratorFilePath() {
        if (extractedFile != null) {
            return extractedFile.toString();
        }

        InputStream is = ExportsStorage.class.getResourceAsStream("/DefFileGenerator.exe");
        OutputStream os = null;

        byte[] buffer = new byte[1024];
        int readBytes;
        try {
            extractedFile = File.createTempFile("DefFileGenerator", ".exe");
            extractedFile.deleteOnExit();
            os = new FileOutputStream(extractedFile);
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } catch (IOException ex) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                is.close();
            } catch (IOException ex) {
            }
        }

        return extractedFile.toString();
    }
}
