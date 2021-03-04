/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides generic information about the openTCS environment.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Environment {

  /**
   * The baseline properties file.
   */
  private static final String BASELINE_PROPS_FILE = "/opentcs.properties";
  /**
   * The customization properties file.
   */
  private static final String CUSTOMIZATION_PROPS_FILE = "/opentcs-customization.properties";
  /**
   * The openTCS baseline version as a string.
   */
  private static final String BASELINE_VERSION;
  /**
   * The build date of the openTCS baseline as a string.
   */
  private static final String BASELINE_BUILD_DATE;
  /**
   * The name of the customized distribution.
   */
  private static final String CUSTOMIZATION_NAME;
  /**
   * The version of the customized distribution as a string.
   */
  private static final String CUSTOMIZATION_VERSION;
  /**
   * The build date of the customized distribution as a string.
   */
  private static final String CUSTOMIZATION_BUILD_DATE;
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

  static {
    Properties props = new Properties();
    try {
      InputStream inStream = Environment.class.getResourceAsStream(BASELINE_PROPS_FILE);
      if (inStream != null) {
        props.load(inStream);
      }
      inStream = Environment.class.getResourceAsStream(CUSTOMIZATION_PROPS_FILE);
      if (inStream != null) {
        props.load(inStream);
      }
    }
    catch (IOException exc) {
      throw new IllegalStateException("Could not load environment properties", exc);
    }
    BASELINE_VERSION = props.getProperty("opentcs.version", "unknown version");
    BASELINE_BUILD_DATE = props.getProperty("opentcs.builddate", "unknown build date");
    CUSTOMIZATION_NAME = props.getProperty("opentcs.customization.name", "-");
    CUSTOMIZATION_VERSION = props.getProperty("opentcs.customization.version", "-");
    CUSTOMIZATION_BUILD_DATE = props.getProperty("opentcs.customization.builddate", "-");
  }

  /**
   * Prevents undesired instantiation.
   */
  private Environment() {
  }

  /**
   * Returns the version of openTCS (i.e. the base library) as a string.
   *
   * @return The version of openTCS (i.e. the base library) as a string.
   */
  public static String getBaselineVersion() {
    return BASELINE_VERSION;
  }

  /**
   * Returns the build date of openTCS (i.e. the base library) as a string.
   *
   * @return The build date of openTCS (i.e. the base library) as a string.
   */
  public static String getBaselineBuildDate() {
    return BASELINE_BUILD_DATE;
  }

  /**
   * Returns the name of the customized distribution.
   *
   * @return The name of the customized distribution.
   */
  public static String getCustomizationName() {
    return CUSTOMIZATION_NAME;
  }

  /**
   * Returns the version of the customized distribution as a string.
   *
   * @return The version of the customized distribution as a string.
   */
  public static String getCustomizationVersion() {
    return CUSTOMIZATION_VERSION;
  }

  /**
   * Returns the build date of the customized distribution as a string.
   *
   * @return The build date of the customized distribution as a string.
   */
  public static String getCustomizationBuildDate() {
    return CUSTOMIZATION_BUILD_DATE;
  }

  /**
   * Write information about the OpenTCS version, the operating system and
   * the running Java VM to the log.
   */
  public static void logSystemInfo() {
    LOG.info("openTCS baseline version: {} (build date: {}), "
        + "customization '{}' version {} (build date: {}), "
        + "Java: {}, {}; JVM: {}, {}; OS: {}, {}",
             BASELINE_VERSION,
             BASELINE_BUILD_DATE,
             CUSTOMIZATION_NAME,
             CUSTOMIZATION_VERSION,
             CUSTOMIZATION_BUILD_DATE,
             System.getProperty("java.version"),
             System.getProperty("java.vendor"),
             System.getProperty("java.vm.version"),
             System.getProperty("java.vm.vendor"),
             System.getProperty("os.name"),
             System.getProperty("os.arch"));
  }
}
