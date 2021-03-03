/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui;

import java.awt.Image;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Static methods related to window icons.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public final class Icons {

  /**
   * This class's logger.
   */
  private static final Logger log = Logger.getLogger(Icons.class.getName());
  /**
   * Path to the openTCS window icons.
   */
  private static final String iconPath = "/org/opentcs/util/gui/res/icons/";
  /**
   * File names of the openTCS window icons.
   */
  private static final String[] iconFiles = {"opentcs_icon_016.png",
                                             "opentcs_icon_032.png",
                                             "opentcs_icon_064.png",
                                             "opentcs_icon_128.png",
                                             "opentcs_icon_256.png"};
  
  /**
   * Prevents instantiation.
   */
  private Icons() {
    // Do nada.
  }

  /**
   * Get the icon for OpentTCS windows in different resolutions.
   *
   * @return List of icons
   */
  public static List<Image> getOpenTCSIcons() {
    List<Image> icons = new LinkedList<>();
    try {
      for (String iconFile : iconFiles) {
        String iconURL = iconPath + iconFile;
        final Image icon = ImageIO.read(Icons.class.getResource(iconURL));
        icons.add(icon);
      }
    }
    catch (IOException | IllegalArgumentException exc) {
      log.log(Level.WARNING, "Couldn't load icon image", exc);
      icons = new LinkedList<>();
    }
    return icons;
  }
}
