/*
 *
 * Created on 20.08.2013 11:50:30
 */
package org.opentcs.guing.util;

import java.util.List;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.util.gui.plugins.LocationTheme;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocationThemeManager {
  /**
   * Sets the theme property and updates the default factory.
   *
   * @param property The theme property.
   */
  void setThemeProperty(LocationThemeProperty property);

  /**
   * Returns the currently used layout theme.
   *
   * @return The theme.
   */
  LocationTheme getDefaultTheme();

  /**
   * Returns the currently used config store theme.
   *
   * @return The theme.
   */
  LocationTheme getDefaultConfigStoreTheme();

  /**
   * Returns all available themes.
   *
   * @return List of themes.
   */
  List<LocationTheme> getThemes();

  /**
   * Update the default theme to the given theme. Called from the client.
   *
   * @param theme The theme.
   */
  void updateDefaultTheme(LocationTheme theme);

  /**
   * Updates the default theme to the theme contained in the
   * <code>ThemeProperty</code>.
   * Called from the <code>VisualLayout</code>.
   */
  void updateDefaultTheme();
}
