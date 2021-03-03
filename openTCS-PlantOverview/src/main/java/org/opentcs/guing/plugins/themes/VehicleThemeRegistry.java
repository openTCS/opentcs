/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.themes;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all vehicle themes in the class path.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public final class VehicleThemeRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleThemeRegistry.class);
  /**
   * The registered themes.
   */
  private final List<VehicleTheme> themes = new LinkedList<>();

  /**
   * Creates a new registry.
   * 
   * @param themes The themes.
   */
  @Inject
  public VehicleThemeRegistry(Set<VehicleTheme> themes) {
    requireNonNull(themes, "themes");
    
    for (VehicleTheme theme : themes) {
      this.themes.add(theme);
      LOG.debug("Found vehicle theme: {}", theme.getClass().getName());
    }

    if (this.themes.isEmpty()) {
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
