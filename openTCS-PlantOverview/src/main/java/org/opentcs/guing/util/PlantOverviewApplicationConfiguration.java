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
import org.opentcs.guing.exchange.ConnectionParamSet;
import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

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
      type = "Boolean",
      description = "Whether reported precise positions should be ignored displaying vehicles."
  )
  boolean ignoreVehiclePrecisePosition();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether reported orientation angles should be ignored displaying vehicles.")
  boolean ignoreVehicleOrientationAngle();

  @ConfigurationEntry(
      type = "String",
      description = {"The plant overview application's locale.",
                     "Valid values: 'English', 'German'"})
  String language();

  @ConfigurationEntry(
      type = "Integer",
      description = "Whether the GUI window should be maximized on startup.")
  boolean frameMaximized();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured height in pixels.")
  int frameBoundsHeight();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured width in pixels.")
  int frameBoundsWidth();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured x-coordinate on screen in pixels.")
  int frameBoundsX();

  @ConfigurationEntry(
      type = "Integer",
      description = "The GUI window's configured y-coordinate on screen in pixels.")
  int frameBoundsY();

  @ConfigurationEntry(
      type = "List of <hostname:port>",
      description = "The configured connection bookmarks.")
  List<ConnectionParamSet> connectionBookmarks();

  @ConfigurationEntry(
      type = "Class name",
      description = {"The name of the class to be used for the location theme.",
                     "Must be a class extending org.opentcs.components.plantoverview.LocationTheme"}
  )
  Class<? extends LocationTheme> locationThemeClass();

  @ConfigurationEntry(
      type = "Class name",
      description = {"The name of the class to be used for the vehicle theme.",
                     "Must be a class extending org.opentcs.components.plantoverview.VehicleTheme"})
  Class<? extends VehicleTheme> vehicleThemeClass();
}
