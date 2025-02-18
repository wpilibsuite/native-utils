package edu.wpi.first.nativeutils.vendordeps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.undercouch.gradle.tasks.download.DownloadAction;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;

/**
 * A task type for downloading vendordep JSON files from the vendor URL.
 */
public class VendorDepTask extends DefaultTask {
    private String url;
    private boolean update;
    private WPIVendorDepsExtension wpiExt = getProject().getExtensions().getByType(WPIVendorDepsExtension.class);

    @Option(option = "url", description = "The vendordep JSON URL or path")
    public void setURL(String url) {
        this.url = url;
    }

    @Option(option = "update", description = "Update the existing vendordeps")
    public void update() {
      update = true;
    }

    /**
     * Installs the JSON file
     * @throws java.io.IOException throws on ioexception
     */
    @TaskAction
    public void install() throws IOException {
        if (update) {
          Gson gson = new GsonBuilder().create();
          Object property = getProject().findProperty(WPIVendorDepsExtension.NATIVEUTILS_VENDOR_FOLDER_PROPERTY);
          File destfolder = new File(property != null ? (String)property : WPIVendorDepsExtension.DEFAULT_VENDORDEPS_FOLDER_NAME);
          File[] vendordeps = destfolder.listFiles();
          if (vendordeps != null) {
            for (File vendordep : vendordeps) {
              getLogger().info("Remotely fetching " + vendordep.toString());
              BufferedReader reader = Files.newBufferedReader(Path.of(vendordep.getPath()));
              var jsonUrl = gson.fromJson(reader, JsonDependency.class).jsonUrl;
              if (jsonUrl != null) {
                if (jsonUrl.isEmpty()) {
                  getLogger().warn("Couldn't get jsonUrl for " + vendordep);
                  continue;
                }
                downloadRemote(Path.of(vendordep.getPath()), jsonUrl);
              } else {
                getLogger().warn("Couldn't get jsonUrl for " + vendordep);
              }
            }
          } else {
            getLogger().warn("Couldn't update vendordeps, invalid directory.");
          }
        } else {
          String filename = findFileName(url);
          Path dest = computeDest(filename);
          if (url.startsWith("FRCLOCAL/")) {
              getLogger().info("Locally fetching $filename");
              copyLocal(filename, dest);
          } else {
              getLogger().info("Remotely fetching " + filename);
              downloadRemote(dest, url);
          }

          var destString = dest.toString();
          String newFilename;
          try (BufferedReader reader = Files.newBufferedReader(dest)) {
              newFilename = new GsonBuilder().create().fromJson(reader, JsonDependency.class).fileName;
              if (newFilename == null) {
                getLogger().warn("Couldn't find fileName field in " + destString + "\n Aborting");
                return;
              }
          } catch (IOException e) {
              throw new RuntimeException(e);
          }
          File file = new File(destString);
          int lastPathSeparator = dest.toString().lastIndexOf('/');
          File newFile = new File(dest.toString().substring(0, lastPathSeparator + 1) + newFilename);
          boolean didRename = file.renameTo(newFile);
          if (didRename) {
            getLogger().info("Succesfully renamed " + file.toString() + " to " + newFile.toString());
          } else {
            getLogger().warn("Failed to rename file " + file.toString() + " to " + newFile.toString());
          }
        }
    }

    /**
     * Find the name of the JSON file.
     * @param inputUrl the vendor JSON URL
     * @return the name of the JSON file, with the `.json` suffix
     */
    private static String findFileName(String inputUrl) {
        if (inputUrl == null) {
            throw new IllegalArgumentException(
                    "No valid vendor JSON URL was entered. Try the following:\n\tgradlew vendordep --url=<insert_url_here>\n" +
                            "Use either a URL to fetch a remote JSON file or `FRCLOCAL/Filename.json` to fetch from the local wpilib folder."
            );
        }
        int lastUrlSeparator = inputUrl.lastIndexOf('/');
        if (lastUrlSeparator == -1) {
            throw new IllegalArgumentException(
                    "Invalid vendor JSON URL was entered. Try the following:\n\tgradlew vendordep --url=<insert_url_here>\n" +
                            "Use either a URL to fetch a remote JSON file or `FRCLOCAL/Filename.json` to fetch from the local wpilib folder."
            );
        }
        return inputUrl.substring(lastUrlSeparator + 1);
    }

    private Path computeDest(String filename) {
        Object property = getProject().findProperty(WPIVendorDepsExtension.NATIVEUTILS_VENDOR_FOLDER_PROPERTY);
        // find project vendordeps folder
        String destfolder = property != null ? (String)property : WPIVendorDepsExtension.DEFAULT_VENDORDEPS_FOLDER_NAME;

        return getProject().file(destfolder).toPath().resolve(filename);
    }

    /**
     * Fetch and copy a vendor JSON from `FRCHOME/vendordeps`
     * @param filename the vendor JSON file name
     * @param dest the destination file
     */
    private void copyLocal(String filename, Path dest) {
        Directory localCache = wpiExt.getFrcHome().dir("vendordeps").get();
        File localFolder = localCache.getAsFile();
        if (!localFolder.isDirectory()) {
            getLogger().error("For some reason " + localFolder + " is not a folder");
            return;
        }

        File[] matches = localFolder.listFiles((dir, name) -> {
            return name.equals(filename);
        });

        // no matches means that source file doesn't exist
        if (matches.length < 1) {
            getLogger().error("Vendordep file " + filename + " was not found in local wpilib vendordep folder (" + localCache.toString() + ").");
            return;
        }

        // only one match could have been found
        Path src = matches[0].toPath();
        getLogger().info("Copying file " + filename + " from " + src.toString() + " to " + dest.toString());
        try {
            if (dest.toFile().exists()) {
                getLogger().warn("Destination file " + filename + " exists and is being overwritten.");
            }
            Path result = Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            getLogger().info("Successfully copied " + filename + " to " + result);
        } catch (IOException ex) {
            getLogger().error(ex.toString());
        }
    }

    /**
     * Download a vendor JSON file from a URL
     * @param dest the destination file
     */
    private void downloadRemote(Path dest, String url) throws IOException {
        DownloadAction downloadAction = new DownloadAction(getProject());
        downloadAction.src(url);
        downloadAction.dest(dest.toFile());
        downloadAction.execute();
    }
}
