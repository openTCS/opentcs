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
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.guing.components.properties.type.VehicleThemeProperty;
import org.opentcs.guing.plugins.themes.VehicleThemeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the registered vehicle themes.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultVehicleThemeManager
    implements VehicleThemeManager {

  /**
   * This class's logger.
   */
  private static final Logger logger
      = LoggerFactory.getLogger(DefaultVehicleThemeManager.class);
  /**
   * The application's configuration.
   */
  private final ApplicationConfiguration appConfig;
  /**
   * The available themes.
   */
  private final List<VehicleTheme> themes;
  /**
   * The default theme.
   */
  private VehicleTheme defaultTheme;
  /**
   * Default theme saved by the client.
   */
  private VehicleTheme defaultConfigStoreTheme;
  /**
   * The theme property defined by the visual layout.
   */
  private VehicleThemeProperty themeProperty;

  /**
   * Creates a new instance.
   *
   * @param registry Provides the registered vehicle themes.
   */
  @Inject
  private DefaultVehicleThemeManager(ApplicationConfiguration appConfig,
                                     VehicleThemeRegistry registry) {
    this.appConfig = requireNonNull(appConfig, "appConfig");
    requireNonNull(registry, "registry");
    this.themes = registry.getThemes();

    evaluateClientDefaultTheme();
  }

  @Override
  public List<VehicleTheme> getThemes() {
    return themes;
  }

  @Override
  public void setThemeProperty(VehicleThemeProperty property) {
    defaultTheme = null;
    defaultConfigStoreTheme = null;
    themeProperty = property;
    String configStoreValue = appConfig.getVehicleThemeName();

    if (!configStoreValue.isEmpty()) {
      for (VehicleTheme theme : themes) {
        if (theme.getName().equals(configStoreValue)) {
          defaultTheme = theme;
          defaultConfigStoreTheme = theme;
        }
      }
    }
    else {
      for (VehicleTheme theme : themes) {
        if (themeProperty != null
            && theme.getClass().getName().equals(themeProperty.getTheme())) {
          defaultTheme = theme;
        }
      }
    }

    if (defaultTheme == null) {
      if (!themes.isEmpty()) {
        logger.warn("Theme with name {} not found. Using {}",
                    themeProperty == null ? null : themeProperty.getTheme(),
                    themes.get(0).getClass().getName());
        defaultTheme = themes.get(0);
      }
      else {
        logger.error("Theme with name {} not found and no other factory available.",
                     themeProperty.getTheme());
      }
    }
  }

  @Override
  public void updateDefaultTheme(VehicleTheme theme) {
    if (themes.contains(theme)) {
      defaultTheme = theme;
      appConfig.setVehicleThemeName(theme.getName());
    }
    else {
      appConfig.setVehicleThemeName("");
      updateDefaultTheme();
    }
  }

  @Override
  public void updateDefaultTheme() {
    setThemeProperty(themeProperty);
  }

  @Override
  public VehicleTheme getDefaultTheme() {
    return defaultTheme;
  }

  @Override
  public VehicleTheme getDefaultConfigStoreTheme() {
    return defaultConfigStoreTheme;
  }

  private void evaluateClientDefaultTheme() {
    String configStoreValue = appConfig.getVehicleThemeName();
    if (!configStoreValue.isEmpty()) {
      for (VehicleTheme theme : themes) {
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
