/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data;

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
   * A property key for {@link org.opentcs.data.model.visualization.VisualLayout}
   * instances used to provide a hint for which
   * {@link org.opentcs.util.gui.plugins.LocationTheme} implementation should be
   * used for rendering locations in the visualization client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of
   * {@link org.opentcs.util.gui.plugins.LocationTheme})
   * </p>
   */
  String LOCATION_THEME_CLASS = "tcs:locationThemeClass";
  /**
   * A property key for {@link org.opentcs.data.model.LocationType} instances
   * used to provide a hint for the visualization how locations of the type
   * should be visualized.
   * <p>
   * Type: String (any element of
   * {@link org.opentcs.data.model.visualization.LocationRepresentation})
   * </p>
   */
  String LOCTYPE_DEFAULT_REPRESENTATION = "tcs:defaultLocationTypeSymbol";
  /**
   * A property key for {@link org.opentcs.data.model.Location} instances used
   * to provide a hint for the visualization how the locations should be
   * visualized.
   * <p>
   * Type: String (any element of
   * {@link org.opentcs.data.model.visualization.LocationRepresentation})
   * </p>
   */
  String LOC_DEFAULT_REPRESENTATION = "tcs:defaultLocationSymbol";
  /**
   * A property key for {@link org.opentcs.data.model.Vehicle} instances to
   * store a preferred initial position to be used by simulating communication
   * adapter, for example.
   * <p>
   * Type: String (any name of a {@link org.opentcs.data.model.Point} existing
   * in the same model.
   * </p>
   */
  String VEHICLE_INITIAL_POSITION = "tcs:initialVehiclePosition";
  /**
   * A property key for {@link org.opentcs.data.model.visualization.VisualLayout}
   * instances used to provide a hint for which
   * {@link org.opentcs.util.gui.plugins.VehicleTheme} implementation should be
   * used for rendering vehicles in the visualization client.
   * <p>
   * Type: String (the fully qualified class name of an implementation of
   * {@link org.opentcs.util.gui.plugins.VehicleTheme})
   * </p>
   */
  String VEHICLE_THEME_CLASS = "tcs:vehicleThemeClass";
  /**
   * A property key for {@link org.opentcs.data.model.Vehicle} instances used to
   * provide the amount of energy (in W) the vehicle consumes during movement to
   * the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  String VIRTUAL_VEHICLE_MOVEMENT_ENERGY = "tcs:virtualVehicleMovementEnergy";
  /**
   * A property key for {@link org.opentcs.data.model.Vehicle} instances used to
   * provide the amount of energy (in W) the vehicle consumes during operations
   * to the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  String VIRTUAL_VEHICLE_OPERATION_ENERGY = "tcs:virtualVehicleOperationEnergy";
  /**
   * A property key for {@link org.opentcs.data.model.Vehicle} instances 
   * used to provide the amount of energy (in W) the vehicle consumes when idle
   * to the loopback driver.
   * <p>
   * Type: Double
   * </p>
   */
  String VIRTUAL_VEHICLE_IDLE_ENERGY = "tcs:virtualVehicleIdleEnergy";
}
