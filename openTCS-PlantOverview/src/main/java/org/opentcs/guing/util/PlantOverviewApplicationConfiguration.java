/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.util.List;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;
import org.opentcs.util.gui.dialog.ConnectionParamSet;

/**
 * Provides methods to configure the PlantOverview application.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(PlantOverviewApplicationConfiguration.PREFIX)
public interface PlantOverviewApplicationConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "plantoverviewapp";

  @ConfigurationEntry(
      type = "String",
      description = {"The plant overview application's locale.",
                     "Valid values: 'English', 'German'"},
      orderKey = "0_init_0")
  String language();

  @ConfigurationEntry(
      type = "String",
      description = {"The plant overview application's mode on startup.",
                     "Valid values: 'MODELLING', 'OPERATING'"},
      orderKey = "0_init_1")
  InitialMode initialMode();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether the GUI window should be maximized on startup.",
      orderKey = "1_size_0")
  boolean frameMaximized();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured width in pixels.",
      orderKey = "1_size_1")
  int frameBoundsWidth();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured height in pixels.",
      orderKey = "1_size_2")
  int frameBoundsHeight();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured x-coordinate on screen in pixels.",
      orderKey = "1_size_3")
  int frameBoundsX();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured y-coordinate on screen in pixels.",
      orderKey = "1_size_4")
  int frameBoundsY();

  @ConfigurationEntry(
      type = "List of <description>\\|<hostname>\\|<port>",
      description = "The configured connection bookmarks.",
      orderKey = "2_connection_0")
  List<ConnectionParamSet> connectionBookmarks();


  @ConfigurationEntry(
      type = "Boolean",
      description = {"Whether to use the configured bookmarks when connecting to the kernel.",
                     "If 'true', the first connection bookmark will be used for the connection "
                     + "attempt.",
                     "If 'false', a dialog will be shown to enter connection parameters."},
      orderKey = "2_connection_1")
  boolean useBookmarksWhenConnecting();

  @ConfigurationEntry(
      type = "Class name",
      description = {"The name of the class to be used for the location theme.",
                     "Must be a class extending org.opentcs.components.plantoverview.LocationTheme"},
      orderKey = "3_themes_0"
  )
  Class<? extends LocationTheme> locationThemeClass();

  @ConfigurationEntry(
      type = "Class name",
      description = {"The name of the class to be used for the vehicle theme.",
                     "Must be a class extending org.opentcs.components.plantoverview.VehicleTheme"},
      orderKey = "3_themes_0")
  Class<? extends VehicleTheme> vehicleThemeClass();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether reported precise positions should be ignored displaying vehicles.",
      orderKey = "4_behaviour_0"
  )
  boolean ignoreVehiclePrecisePosition();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether reported orientation angles should be ignored displaying vehicles.",
      orderKey = "4_behaviour_1")
  boolean ignoreVehicleOrientationAngle();

  enum InitialMode {
    MODELLING,
    OPERATING,
    ASK
  }
}
