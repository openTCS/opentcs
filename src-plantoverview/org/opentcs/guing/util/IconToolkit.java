/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.util;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * A utility class for loading icons.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class IconToolkit {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(IconToolkit.class.getName());
  /**
   * The default path for icons.
   */
  private static final String fDefaultPath = "/org/opentcs/guing/res/symbols/";
  /**
   * The single instance of this class.
   */
  private static IconToolkit fInstance;

  /**
   * Creates a new instance.
   */
  private IconToolkit() {
  }

  /**
   * Returns the single instance of this class.
   *
   * @return The single instance of this class.
   */
  public static IconToolkit instance() {
    if (fInstance == null) {
      fInstance = new IconToolkit();
    }

    return fInstance;
  }

  /**
   * Creates an ImageIcon.
   *
   * @param fullPath The full (absolute) path of the icon file.
   * @return The icon, or <code>null</code>, if the file does not exist.
   */
  public ImageIcon getImageIconByFullPath(String fullPath) {
    URL url = getClass().getResource(fullPath);

    if (url != null) {
      return new ImageIcon(url);
    }
    else {
      log.log(Level.WARNING, "Icon not found: " + fullPath);
      return null;
    }
  }

  /**
   * Creates an ImageIcon.
   *
   * @param relativePath The relative path of the icon file.
   * @return The icon, or <code>null</code>, if the file does not exist.
   */
  public ImageIcon createImageIcon(String relativePath) {
    return getImageIconByFullPath(fDefaultPath + relativePath);
  }
}
