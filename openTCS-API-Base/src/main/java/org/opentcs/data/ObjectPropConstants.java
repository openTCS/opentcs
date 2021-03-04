/**
 * Copyright (c) The openTCS Authors.
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
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Defines some reserved/commonly used property keys and values.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ObjectPropConstants {

  /**
   * A property key for models used to store the last-modified time stamp.
   * <p>
   * Type: A time stamp, encoded using ISO 8601. (Can be parsed using {@code java.time.Instant}.)
   * </p>
   */
  String MODEL_FILE_LAST_MODIFIED = "tcs:modelFileLastModified";
  /**
   * A property key for the orientation of a vehicle on a path.
   * <p>
   * Type: String (any string - details currently not specified)
   * </p>
   *
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  String PATH_TRAVEL_ORIENTATION = "tcs:travelOrientation";
  /**
   * A property key for {@link VisualLayout} instances used to provide a hint for which
   * {@link LocationTheme} implementation should be used for rendering locations in the
   * visualization client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of {@link LocationTheme})
   * </p>
   *
   * @deprecated The theme to be used is now set directly via configuration.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
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
   * 
   * @deprecated Use vehicle driver-specific properties to specify the vehicle's initial position.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  String VEHICLE_INITIAL_POSITION = "tcs:initialVehiclePosition";
  /**
   * A property key for {@link VisualLayout} instances used to provide a hint for which
   * {@link VehicleTheme} implementation should be used for rendering vehicles in the visualization
   * client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of {@link VehicleTheme})
   * </p>
   *
   * @deprecated The theme to be used is now set directly via configuration.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0", details = "Will be removed.")
  String VEHICLE_THEME_CLASS = "tcs:vehicleThemeClass";
}
