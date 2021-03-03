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
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.guing.components.properties.type.LocationThemeProperty;
import org.opentcs.guing.plugins.themes.LocationThemeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the registered location themes.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultLocationThemeManager
    implements LocationThemeManager {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(DefaultLocationThemeManager.class);
  /**
   * The application's configuration.
   */
  private final ApplicationConfiguration appConfig;
  /**
   * The available themes.
   */
  private final List<LocationTheme> themes;
  /**
   * ???
   */
  private LocationThemeProperty themeProperty;
  /**
   * The default factory.
   */
  private LocationTheme defaultTheme;
  /**
   * ???
   */
  private LocationTheme defaultConfigStoreTheme;

  /**
   * Creates a new instance.
   *
   * @param registry Provides all registered location themes.
   */
  @Inject
  private DefaultLocationThemeManager(ApplicationConfiguration appConfig,
                                      LocationThemeRegistry registry) {
    this.appConfig = requireNonNull(appConfig, "appConfig");
    requireNonNull(registry, "registry");
    this.themes = registry.getThemes();

    evaluateClientDefaultTheme();
  }

  @Override
  public void setThemeProperty(LocationThemeProperty property) {
    defaultTheme = null;
    defaultConfigStoreTheme = null;
    themeProperty = property;
    String configStoreValue = appConfig.getLocationThemeName();

    if (!configStoreValue.isEmpty()) {
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
        log.warn("Theme with name {} not found and no other factory available.", themeProperty.getTheme());
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
      appConfig.setLocationThemeName(theme.getName());
    }
    else {
      appConfig.setLocationThemeName("");
      updateDefaultTheme();
    }
  }

  @Override
  public void updateDefaultTheme() {
    setThemeProperty(themeProperty);
  }

  private void evaluateClientDefaultTheme() {
    String configStoreValue = appConfig.getLocationThemeName();

    if (!configStoreValue.isEmpty()) {
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
