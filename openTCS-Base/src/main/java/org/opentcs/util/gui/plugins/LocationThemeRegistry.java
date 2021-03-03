/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * A registry for all location themes in the class path.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public final class LocationThemeRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(LocationThemeRegistry.class.getName());
  /**
   * The registered themes.
   */
  private final List<LocationTheme> themes = new LinkedList<>();

  /**
   * Creates a new registry.
   */
  public LocationThemeRegistry() {
    // Auto-detect location theme factories.
    for (LocationTheme theme : ServiceLoader.load(LocationTheme.class)) {
      themes.add(theme);
      log.fine("Found location theme: " + theme.getClass().getName());
    }

    if (themes.isEmpty()) {
      throw new IllegalStateException("No location themes found.");
    }
  }

  /**
   * Returns all registered location themes.
   *
   * @return All registered location themes.
   */
  public List<LocationTheme> getThemes() {
    return new LinkedList<>(themes);
  }
}
