/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.VisualLayout;

/**
 * Defines some reserved/commonly used property keys and values.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ObjectPropConstants {

  /**
   * A property key for the orientation of a vehicle on a path.
   * <p>
   * Type String (any string - details currently not specified)
   * </p>
   */
  String PATH_TRAVEL_ORIENTATION = "tcs:travelOrientation";
  /**
   * A property key for {@link VisualLayout} instances used to provide a hint for which
   * {@link LocationTheme} implementation should be used for rendering locations in the
   * visualization client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of {@link LocationTheme})
   * </p>
   */
  String LOCATION_THEME_CLASS = "tcs:locationThemeClass";
  /**
   * A property key for {@link LocationType} instances used to provide a hint for the visualization
   * how locations of the type should be visualized.
   * <p>
   * Type: String (any element of {@link LocationRepresentation})
   * </p>
   */
  String LOCTYPE_DEFAULT_REPRESENTATION = "tcs:defaultLocationTypeSymbol";
  /**
   * A property key for {@link Location} instances used to provide a hint for the visualization how
   * the locations should be visualized.
   * <p>
   * Type: String (any element of {@link LocationRepresentation})
   * </p>
   */
  String LOC_DEFAULT_REPRESENTATION = "tcs:defaultLocationSymbol";
  /**
   * A property key for {@link Vehicle} instances to store a preferred initial position to be used
   * by simulating communication adapter, for example.
   * <p>
   * Type: String (any name of a {@link Point} existing in the same model.
   * </p>
   */
  String VEHICLE_INITIAL_POSITION = "tcs:initialVehiclePosition";
  /**
   * A property key for {@link VisualLayout} instances used to provide a hint for which
   * {@link VehicleTheme} implementation should be used for rendering vehicles in the visualization
   * client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of {@link VehicleTheme})
   * </p>
   */
  String VEHICLE_THEME_CLASS = "tcs:vehicleThemeClass";
}
