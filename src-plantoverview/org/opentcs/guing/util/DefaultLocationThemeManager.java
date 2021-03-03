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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.plugins.LocationTheme;
import org.opentcs.util.gui.plugins.LocationThemeRegistry;

/**
 * Provides utility for location themes.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultLocationThemeManager
    implements LocationThemeManager {

  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(DefaultLocationThemeManager.class.getName());
  private static final DefaultLocationThemeManager instance
      = new DefaultLocationThemeManager();
  private static final String CONFIGSTOREKEY
      = ConfigConstants.LOCATION_THEME;
  private static LocationThemeProperty themeProperty;
  private final List<LocationTheme> themes;
  /**
   * The default factory.
   */
  private LocationTheme defaultTheme;
  private LocationTheme defaultConfigStoreTheme;

  private DefaultLocationThemeManager() {
    this.themes = (new LocationThemeRegistry()).getThemes();
    this.evaluateClientDefaultTheme();
  }

  /**
   * Returns the instance of this ThemeManager.
   *
   * @return The ThemeManager.
   */
  public static DefaultLocationThemeManager getInstance() {
    return instance;
  }

  @Override
  public void setThemeProperty(LocationThemeProperty property) {
    defaultTheme = null;
    defaultConfigStoreTheme = null;
    themeProperty = property;
    String configStoreValue = configStore.getString(CONFIGSTOREKEY, "undefined");

    if (!configStoreValue.equals("undefined")) {
      for (LocationTheme theme : themes) {
        if (theme.getName().equals(configStoreValue)) {
          defaultTheme = theme;
          defaultConfigStoreTheme = theme;
        }
      }
    }
    else {
      for (LocationTheme theme : themes) {
        if (themeProperty != null
            && theme.getClass().getName().equals(themeProperty.getTheme())) {
          defaultTheme = theme;
        }
      }
    }

    if (defaultTheme == null) {
      if (!themes.isEmpty()) {
        defaultTheme = themes.get(0);
      }
      else {
        log.log(Level.WARNING, "Theme with name {0} not found and no other factory available.", themeProperty.getTheme());
      }
    }
  }

  @Override
  public LocationTheme getDefaultTheme() {
    return defaultTheme;
  }

  @Override
  public LocationTheme getDefaultConfigStoreTheme() {
    return defaultConfigStoreTheme;
  }

  @Override
  public List<LocationTheme> getThemes() {
    return themes;
  }

  @Override
  public void updateDefaultTheme(LocationTheme theme) {
    if (themes.contains(theme)) {
      defaultTheme = theme;
      configStore.setString(CONFIGSTOREKEY, theme.getName());
    }
    else {
      configStore.setString(CONFIGSTOREKEY, "undefined");
      updateDefaultTheme();
    }
  }

  @Override
  public void updateDefaultTheme() {
    setThemeProperty(themeProperty);
  }

  private void evaluateClientDefaultTheme() {
    String configStoreValue = configStore.getString(CONFIGSTOREKEY, "undefined");

    if (!configStoreValue.equals("undefined")) {
      for (LocationTheme theme : themes) {
        if (theme.getName().equals(configStoreValue)) {
          defaultTheme = theme;
          defaultConfigStoreTheme = theme;
        }
      }
    }
    else {
      updateDefaultTheme();
    }
  }
}
