// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides methods for loading resource files.
 */
public class ResourceLoader {

  private ResourceLoader() {
  }

  /**
   * Loads the content of the resource file with the given path and returns it as a string.
   *
   * @param path The path to the file to load.
   * @return The content of the file as a string.
   */
  public static String load(String path) {
    try (InputStream is = ResourceLoader.class.getResourceAsStream(path)) {
      return new String(is.readAllBytes());
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to load resource: " + path, e);
    }
  }
}
