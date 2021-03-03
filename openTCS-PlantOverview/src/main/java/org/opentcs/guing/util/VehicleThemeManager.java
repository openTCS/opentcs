/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.util;

import java.util.List;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;

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
