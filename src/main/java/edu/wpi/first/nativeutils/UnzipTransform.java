package edu.wpi.first.nativeutils;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.internal.UncheckedException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provides a generic transform from a zipped file to an extracted directory.  The extracted directory
 * is located in the output directory of the transform and is named after the zipped file name
 * minus the extension.
 */
public interface UnzipTransform extends TransformAction<TransformParameters.None> {

    // TODO see if we can get incremental to work

    @InputArtifact
    Provider<FileSystemLocation> getZippedFile();

    @Override
    default void transform(TransformOutputs outputs) {
        File zippedFile = getZippedFile().get().getAsFile();
        String unzippedDirName = FileUtils.removeExtension(zippedFile.getName());
        File unzipDir = outputs.dir(unzippedDirName);
        try {
            unzipTo(zippedFile, unzipDir);
        } catch (IOException e) {
            throw UncheckedException.throwAsUncheckedException(e);
        }
    }

    static void unzipTo(File headersZip, File unzipDir) throws IOException {
        try (ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(headersZip)))) {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File outFile = new File(unzipDir, entry.getName());
                FileUtils.createParentDirs(outFile);
                try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
                    FileUtils.copyLarge(inputStream, outputStream);
                }
            }
        }
    }
}
