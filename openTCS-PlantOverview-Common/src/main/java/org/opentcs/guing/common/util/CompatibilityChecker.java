/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import javax.annotation.Nullable;
import javax.swing.JOptionPane;

/**
 * Provides environment compatibility checks.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompatibilityChecker {

  /**
   * Prevents unwanted instantiation.
   */
  private CompatibilityChecker() {
  }

  /**
   * Checks whether the given Java version string is compatible with the Docking Frames library.
   * Docking Frames expects two periods with an integer between them, e.g. "x.y.z". Version numbers
   * that do not follow this pattern lead to an exception on startup.
   *
   * @param version The version string.
   * @return Whether the version string is compatible with the Docking Frames library.
   */
  public static boolean versionCompatibleWithDockingFrames(@Nullable String version) {
    return version != null && version.matches(".*\\.[0-9]+\\..*");
  }

  /**
   * Shows a message dialog explaining that the Java version is incompatible with Docking Frames.
   */
  public static void showVersionIncompatibleWithDockingFramesMessage() {
    JOptionPane.showMessageDialog(
        null,
        "Your Java Runtime Environment is incompatible with this application.\n"
        + "Please use a different JRE. Recommended: Adoptium (see https://adoptium.net/)",
        "Incompatible Java version",
        JOptionPane.ERROR_MESSAGE
    );
  }
}
