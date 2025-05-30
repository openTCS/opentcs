// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs;

import java.io.File;
import org.opentcs.util.FileSystems;

/**
 * A class that keeps/provides commonly used data about the test environment.
 */
public final class TestEnvironment {

  /**
   * The home directory for the openTCS system during tests.
   */
  private static final File KERNEL_HOME_DIRECTORY;
  /**
   * The directory in which persistent data of the openTCS kernel is stored.
   */
  private static final File KERNEL_DATA_DIRECTORY;
  /**
   * The directory in which log files are kept.
   */
  private static final File LOG_FILE_DIRECTORY;

  static {
    KERNEL_HOME_DIRECTORY
        = new File(System.getProperty("java.io.tmpdir"), "openTCS-Tests");
    KERNEL_DATA_DIRECTORY = new File(KERNEL_HOME_DIRECTORY, "data");
    LOG_FILE_DIRECTORY = new File(System.getProperty("java.io.tmpdir"), "log");
  }

  /**
   * Prevents creation of instances.
   */
  private TestEnvironment() {
  }

  /**
   * Returns the home directory for the openTCS system during tests.
   *
   * @return The home directory for the openTCS system during tests.
   */
  public static File getKernelHomeDirectory() {
    return KERNEL_HOME_DIRECTORY;
  }

  /**
   * Initializes the test environment.
   */
  private static void init() {
    // Clean and recreate the home directory.
    FileSystems.deleteRecursively(KERNEL_HOME_DIRECTORY);
    KERNEL_HOME_DIRECTORY.mkdirs();
    KERNEL_DATA_DIRECTORY.mkdirs();
    LOG_FILE_DIRECTORY.mkdirs();
  }

  /**
   * Initializes the test environment.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    System.out.println("Initializing the test environment...");
    init();
  }
}
