/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.gradle.plugin.integration.util;

import java.util.Map;
import org.gradle.api.Project;

/**
 * Utility class to read properties from a gradle project.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class ProjectPropertyReader {

  /**
   * Reads a property with the given key from the project.
   * If no value is present, the default value will be used.
   *
   * @param project The project
   * @param key The key of the property to look for
   * @param defaultValue The default value of the given key
   * @return The value of the key or the default one
   */
  public static String getProjectProperty(Project project, String key, String defaultValue) {
    Map<String, ?> properties = project.getProperties();
    String prop = (String) properties.get(key);
    if (prop != null && !prop.equals("")) {
      return prop;
    }
    else {
      return defaultValue;
    }
  }
}
