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
import org.opentcs.components.plantoverview.LocationTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for all location themes available.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public final class LocationThemeRegistry {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LocationThemeRegistry.class);
  /**
   * The registered themes.
   */
  private final List<LocationTheme> themes = new LinkedList<>();

  /**
   * Creates a new registry.
   *
   * @param themes The themes.
   */
  @Inject
  public LocationThemeRegistry(Set<LocationTheme> themes) {
    requireNonNull(themes, "themes");

    for (LocationTheme theme : themes) {
      this.themes.add(theme);
      LOG.debug("Found location theme: {}", theme.getClass().getName());
    }

    if (this.themes.isEmpty()) {
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
