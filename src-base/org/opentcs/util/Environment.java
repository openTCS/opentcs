/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides generic information about the openTCS environment.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Environment {

  /**
   * Our properties file.
   */
  private static final String propsFile = "/opentcs.properties";
  /**
   * The openTCS version as a string.
   */
  private static final String versionString;
  /**
   * This class's Logger.
   */
  private static final Logger log = Logger.getLogger(Environment.class.getName());

  static {
    Properties props = new Properties();
    try {
      props.load(Environment.class.getResourceAsStream(propsFile));
    }
    catch (IOException exc) {
      log.log(Level.WARNING,
              "Exception loading properties from " + propsFile,
              exc);
    }
    versionString = props.getProperty("opentcs.version", "unknown version");
  }

  /**
   * Prevents undesired instantiation.
   */
  private Environment() {
    // Do nada.
  }

  /**
   * Returns the openTCS version as a string.
   *
   * @return The openTCS version as a string.
   */
  public static String getVersionString() {
    return versionString;
  }

  /**
   * Write information about the OpenTCS version, the operating system and
   * the running Java VM to the log.
   */
  public static void logSystemInfo() {
    String systemInfo = new StringBuilder()
        .append("OpenTCS: ")
        .append(versionString)
        .append("; ")
        .append("Java: ")
        .append(System.getProperty("java.version"))
        .append(", ")
        .append(System.getProperty("java.vendor"))
        .append("; ")
        .append("JVM: ")
        .append(System.getProperty("java.vm.version"))
        .append(", ")
        .append(System.getProperty("java.vm.vendor"))
        .append("; ")
        .append("OS: ")
        .append(System.getProperty("os.name"))
        .append(", ")
        .append(System.getProperty("os.arch"))
        .toString();
    log.info(systemInfo);
  }
}
