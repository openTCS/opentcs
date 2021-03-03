/*
 *
 * Created on 20.08.2013 11:50:30
 */
package org.opentcs.guing.util;

import java.util.List;
import java.util.Set;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleThemeManager {

  /**
   * Returns all available themes.
   *
   * @return List of themes.
   */
  List<VehicleTheme> getThemes();

  /**
   * Sets the theme property and updates the default factory.
   *
   * @param property The theme property.
   */
  void setThemeProperty(VehicleThemeProperty property);

  /**
   * Returns all image paths used by the themes.
   *
   * @return Set containing all image paths.
   * @deprecated Why is this method necessary? What is it required for?
   */
  @Deprecated
  Set<String> getAllImagePaths();

  /**
   * Update the default theme to the given theme. Called from the client.
   *
   * @param theme The theme.
   */
  void updateDefaultTheme(VehicleTheme theme);

  /**
   * Updates the default theme to the theme contained in the
   * <code>ThemeProperty</code>.
   */
  void updateDefaultTheme();

  /**
   * Returns the currently used theme.
   *
   * @return The currently used theme.
   */
  VehicleTheme getDefaultTheme();

  /**
   * Returns the currently used config store theme.
   *
   * @return The currently used config store theme.
   */
  VehicleTheme getDefaultConfigStoreTheme();
}
