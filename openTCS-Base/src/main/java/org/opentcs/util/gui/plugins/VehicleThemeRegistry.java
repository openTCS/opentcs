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
 * A registry for all vehicle themes in the class path.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public final class VehicleThemeRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(VehicleThemeRegistry.class.getName());
  /**
   * The registered themes.
   */
  private final List<VehicleTheme> themes = new LinkedList<>();

  /**
   * Creates a new registry.
   */
  public VehicleThemeRegistry() {
    // Auto-detect vehicle theme factories.
    for (VehicleTheme theme : ServiceLoader.load(VehicleTheme.class)) {
      themes.add(theme);
      log.fine("Found vehicle theme: " + theme.getClass().getName());
    }

    if (themes.isEmpty()) {
      throw new IllegalStateException("No vehicle themes found.");
    }
  }

  /**
   * Returns all registered vehicle themes.
   *
   * @return All registered vehicle themes.
   */
  public List<VehicleTheme> getThemes() {
    return new LinkedList<>(themes);
  }
}
