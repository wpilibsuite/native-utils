package edu.wpi.first.nativeutils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FileUtils {
  public static final char EXTENSION_SEPARATOR = '.';
  private static final char UNIX_SEPARATOR = '/';
  private static final char WINDOWS_SEPARATOR = '\\';
  public static final int DEFAULT_BUFFER_SIZE = 8192;
  public static final int EOF = -1;

  public static int indexOfLastSeparator(String filename) {
    if (filename == null) {
      return -1;
    }
    int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
  }

  public static int indexOfExtension(String filename) {
    if (filename == null) {
      return -1;
    }
    int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
    int lastSeparator = indexOfLastSeparator(filename);
    return (lastSeparator > extensionPos ? -1 : extensionPos);
  }

  public static String removeExtension(String filename) {
    if (filename == null) {
      return null;
    }
    int index = indexOfExtension(filename);
    if (index == -1) {
      return filename;
    } else {
      return filename.substring(0, index);
    }
  }

  public static void createParentDirs(File file) throws IOException {
    Objects.requireNonNull(file);
    File parent = file.getCanonicalFile().getParentFile();
    if (parent == null) {
      /*
       * The given directory is a filesystem root. All zero of its ancestors exist.
       * This doesn't mean that the root itself exists -- consider x:\ on a Windows
       * machine without such a drive -- or even that the caller can create it, but
       * this method makes no such guarantees even for non-root files.
       */
      return;
    }
    parent.mkdirs();
    if (!parent.isDirectory()) {
      throw new IOException("Unable to create parent directories of " + file);
    }
  }

  public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
      throws IOException {
    return copyLarge(input, output, new byte[bufferSize]);
  }

  public static long copyLarge(final InputStream input, final OutputStream output)
      throws IOException {
    return copy(input, output, DEFAULT_BUFFER_SIZE);
  }

  public static long copyLarge(
      final InputStream input, final OutputStream output, final byte[] buffer) throws IOException {
    long count = 0;
    if (input != null) {
      int n;
      while (EOF != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
        count += n;
      }
    }
    return count;
  }
}
