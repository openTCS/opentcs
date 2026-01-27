// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.gui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static methods related to window icons.
 */
public final class Icons {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Icons.class);
  /**
   * Path to the openTCS window icons.
   */
  private static final String ICON_PATH = "/org/opentcs/util/gui/res/icons/";
  /**
   * File names of the default openTCS window icons.
   */
  private static final List<String> DEFAULT_ICON_FILES = List.of(
      "opentcs_icon_016.png",
      "opentcs_icon_032.png",
      "opentcs_icon_064.png",
      "opentcs_icon_128.png",
      "opentcs_icon_256.png"
  );
  /**
   * File names of custom window icons that can be provided by a user.
   */
  private static final List<String> CUSTOM_ICON_FILES = List.of(
      "custom_icon_016.png",
      "custom_icon_032.png",
      "custom_icon_064.png",
      "custom_icon_128.png",
      "custom_icon_256.png"
  );

  /**
   * Prevents instantiation.
   */
  private Icons() {
    // Do nada.
  }

  /**
   * Get the icon for openTCS windows in different resolutions.
   *
   * @return List of icons
   */
  public static List<Image> getOpenTCSIcons() {
    List<Image> customIcons = loadCustomIcons();
    return customIcons.isEmpty() ? loadDefaultIcons() : customIcons;
  }

  private static List<Image> loadCustomIcons() {
    List<Image> icons = loadIcons(findFiles(ICON_PATH, CUSTOM_ICON_FILES));

    if (icons.isEmpty()) {
      LOG.debug(
          "Couldn't find any custom icon files at '{}' (file names: {}). Using default icon files.",
          ICON_PATH,
          CUSTOM_ICON_FILES
      );
      return List.of();
    }

    if (icons.size() < CUSTOM_ICON_FILES.size()) {
      LOG.warn(
          "Couldn't find all custom icon files at '{}' (file names: {}). Using default icon files.",
          ICON_PATH,
          CUSTOM_ICON_FILES
      );
      return List.of();
    }

    return icons;
  }

  private static List<Image> loadDefaultIcons() {
    return loadIcons(findFiles(ICON_PATH, DEFAULT_ICON_FILES));
  }

  private static List<URL> findFiles(String path, List<String> fileNames) {
    return fileNames.stream()
        .map(fileName -> Icons.class.getResource(path + fileName))
        .filter(Objects::nonNull)
        .toList();
  }

  private static List<Image> loadIcons(List<URL> files) {
    try {
      List<Image> icons = new ArrayList<>();
      for (URL iconFile : files) {
        final Image icon = ImageIO.read(iconFile);
        icons.add(icon);
      }
      return icons;
    }
    catch (IOException exc) {
      LOG.warn("Couldn't load icon images from paths: {}", files, exc);
      return List.of();
    }
  }
}
