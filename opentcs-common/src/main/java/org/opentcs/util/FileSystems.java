// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util;

import static java.util.Objects.requireNonNull;

import java.io.File;

/**
 * This class provides helper methods for working with file systems.
 */
public final class FileSystems {

  /**
   * Prevents creation of instances.
   */
  private FileSystems() {
  }

  /**
   * Recursively deletes a given file/directory.
   *
   * @param target The file/directory to be deleted recursively.
   * @return <code>true</code> if deleting the target was successful, else
   * <code>false</code>.
   */
  public static boolean deleteRecursively(File target) {
    requireNonNull(target, "target");

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
