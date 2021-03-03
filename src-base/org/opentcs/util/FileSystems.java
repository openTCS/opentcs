/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class provides helper methods for working with file systems.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class FileSystems {

  /**
   * The buffer size to use for file copying.
   */
  private static final int copyBufferSize = 1024 * 1024;

  /**
   * Prevents creation of instances.
   */
  private FileSystems() {
  }

  /**
   * Copies a file.
   *
   * @param srcFile The file to be copied.
   * @param dstFile The file to be copied to.
   * @param overwrite If <code>true</code>, the destination file will be
   * overwritten if it already exists.
   * @throws IllegalArgumentException If <code>dstFile</code> exists and
   * <code>overwrite</code> is <code>false</code>.
   * @throws FileNotFoundException XXX document this
   * @throws IOException If an error occurs during copying.
   */
  public static void copyFile(File srcFile, File dstFile, boolean overwrite)
      throws IllegalArgumentException, FileNotFoundException, IOException {
    if (srcFile == null) {
      throw new NullPointerException("srcFile is null");
    }

    if (dstFile == null) {
      throw new NullPointerException("dstFile is null");
    }

    if (dstFile.exists() && !overwrite) {
      throw new IllegalArgumentException(
          "dstFile exists and overwriting is not allowed");
    }

    InputStream inStream =
        new BufferedInputStream(new FileInputStream(srcFile));
    OutputStream outStream =
        new BufferedOutputStream(new FileOutputStream(dstFile));
    byte[] buffer = new byte[copyBufferSize];
    int bytesRead;

    while ((bytesRead = inStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, bytesRead);
    }

    inStream.close();
    outStream.close();
  }

  /**
   * Recursively deletes a given file/directory.
   *
   * @param target The file/directory to be deleted recursively.
   * @return <code>true</code> if deleting the target was successful, else
   * <code>false</code>.
   */
  public static boolean deleteRecursively(File target) {
    if (target == null) {
      throw new NullPointerException("target is null");
    }
    // If the target is a directory, remove its contents first.
    if (target.isDirectory()) {
      File[] entries = target.listFiles();
      for (File curEntry : entries) {
        boolean successful;

        if (curEntry.isDirectory()) {
          successful = deleteRecursively(curEntry);
        }
        else {
          successful = curEntry.delete();
        }

        if (!successful) {
          return false;
        }
      }
    }

    return target.delete();
  }
}
