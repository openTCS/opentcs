/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.File;
import static java.util.Objects.requireNonNull;

/**
 * This class provides helper methods for working with file systems.
 *
 * @author Stefan Walter (Fraunhofer IML)
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
