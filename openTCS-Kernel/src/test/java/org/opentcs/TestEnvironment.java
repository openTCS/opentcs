/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
/*
 *
 * Created on July 25, 2006, 10:30 AM
 */

package org.opentcs;

import java.io.File;
import org.opentcs.util.FileSystems;

/**
 * A class that keeps/provides commonly used data about the test environment.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class TestEnvironment {
  /**
   * The home directory for the openTCS system during tests.
   */
  private static final File kernelHomeDirectory;
  /**
   * The directory in which persistent data of the openTCS kernel is stored.
   */
  private static final File kernelDataDirectory;
  /**
   * The directory in which log files are kept.
   */
  private static final File logFileDirectory;
  
  static {
    kernelHomeDirectory =
          new File(System.getProperty("java.io.tmpdir"), "openTCS-Tests");
    kernelDataDirectory = new File(kernelHomeDirectory, "data");
    logFileDirectory = new File(System.getProperty("java.io.tmpdir"), "log");
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
    return kernelHomeDirectory;
  }
  
  /**
   * Initializes the test environment.
   */
  private static void init() {
    // Clean and recreate the home directory.
    FileSystems.deleteRecursively(kernelHomeDirectory);
    kernelHomeDirectory.mkdirs();
    kernelDataDirectory.mkdirs();
    logFileDirectory.mkdirs();
  }
  
  /**
   * Initializes the test environment.
   */
  public static void main(String[] args) {
    System.out.println("Initializing the test environment...");
    init();
  }
}
