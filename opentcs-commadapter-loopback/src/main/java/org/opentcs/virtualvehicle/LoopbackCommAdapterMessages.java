// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleCommAdapterEvent;

/**
 * Defines message types (and their parameters) supported by the
 * {@link LoopbackCommunicationAdapter}.
 */
public abstract class LoopbackCommAdapterMessages {

  /**
   * A message for initializing a vehicle's position.
   */
  public static final String INIT_POSITION = "tcs:virtualVehicle:initPosition";
  /**
   * A parameter for the position.
   */
  public static final String INIT_POSITION_PARAM_POSITION = "position";

  /**
   * A message to notify the loopback adapter the last/current movement command failed.
   */
  public static final String CURRENT_MOVEMENT_COMMAND_FAILED
      = "tcs:virtualVehicle:currentMovementCommandFailed";

  /**
   * A message to publish {@link VehicleCommAdapterEvent}s.
   */
  public static final String PUBLISH_EVENT = "tcs:virtualVehicle:publishEvent";
  /**
   * A parameter for the event appendix.
   * (Can be omitted for publishing events without any appendix.)
   */
  public static final String PUBLISH_EVENT_PARAM_APPENDIX = "eventAppendix";

  /**
   * A message to set a vehicle's energy level.
   */
  public static final String SET_ENERGY_LEVEL = "tcs:virtualVehicle:setEnergyLevel";
  /**
   * A parameter for the energy level.
   * The parameter's value must be set to an integer value.
   */
  public static final String SET_ENERGY_LEVEL_PARAM_LEVEL = "energyLevel";

  /**
   * A message to set the loaded state of the {@link LoadHandlingDevice} attached to a vehicle.
   */
  public static final String SET_LOADED = "tcs:virtualVehicle:setLoaded";
  /**
   * A parameter for the loaded state.
   * The parameter's value must be set to a boolean value.
   */
  public static final String SET_LOADED_PARAM_LOADED = "loaded";

  /**
   * A message to set a vehicle's orientation angle.
   */
  public static final String SET_ORIENTATION_ANGLE = "tcs:virtualVehicle:setOrientationAngle";
  /**
   * A parameter for the orientation angle.
   * The parameter's value must be set to a floating-point value.
   */
  public static final String SET_ORIENTATION_ANGLE_PARAM_ANGLE = "orientationAngle";

  /**
   * A message to set a vehicle's position.
   */
  public static final String SET_POSITION = "tcs:virtualVehicle:setPosition";
  /**
   * A parameter for the position.
   */
  public static final String SET_POSITION_PARAM_POSITION = "position";

  /**
   * A message to reset a vehicle's position.
   */
  public static final String RESET_POSITION = "tcs:virtualVehicle:resetPosition";

  /**
   * A message to set a vehicle's precise position.
   */
  public static final String SET_PRECISE_POSITION = "tcs:virtualVehicle:setPrecisePosition";
  /**
   * A parameter for the x-position.
   * The parameter's value must be set to a long value.
   */
  public static final String SET_PRECISE_POSITION_PARAM_X = "x";
  /**
   * A parameter for the y-position.
   * The parameter's value must be set to a long value.
   */
  public static final String SET_PRECISE_POSITION_PARAM_Y = "y";
  /**
   * A parameter for the z-position.
   * The parameter's value must be set to a long value.
   */
  public static final String SET_PRECISE_POSITION_PARAM_Z = "z";

  /**
   * A message to reset a vehicle's precise position.
   */
  public static final String RESET_PRECISE_POSITION = "tcs:virtualVehicle:resetPrecisePosition";

  /**
   * A message to set a vehicle's state.
   */
  public static final String SET_STATE = "tcs:virtualVehicle:setState";
  /**
   * A parameter for the state.
   * The parameter's value must be set to the name of the corresponding state.
   */
  public static final String SET_STATE_PARAM_STATE = "state";

  /**
   * A message to pause/unpause a vehicle.
   */
  public static final String SET_PAUSED = "tcs:virtualVehicle:setPaused";
  /**
   * A parameter for the paused state.
   * The parameter's value must be set to a boolean value.
   */
  public static final String SET_PAUSED_PARAM_PAUSED = "paused";

  /**
   * A message to set a vehicle's property.
   */
  public static final String SET_PROPERTY = "tcs:virtualVehicle:setProperty";
  /**
   * A parameter for the key.
   */
  public static final String SET_PROPERTY_PARAM_KEY = "key";
  /**
   * A parameter for the value.
   */
  public static final String SET_PROPERTY_PARAM_VALUE = "value";

  /**
   * A message to reset a vehicle's property.
   */
  public static final String RESET_PROPERTY = "tcs:virtualVehicle:resetProperty";
  /**
   * A parameter for the key.
   */
  public static final String RESET_PROPERTY_PARAM_KEY = "key";

  /**
   * A message to enable/disable the loopback adapter's single step mode.
   */
  public static final String SET_SINGLE_STEP_MODE_ENABLED
      = "tcs:virtualVehicle:setSingleStepModeEnabled";
  /**
   * A parameter for the enabled state.
   * The parameter's value must be set to a boolean value.
   */
  public static final String SET_SINGLE_STEP_MODE_ENABLED_PARAM_ENABLED = "enabled";

  /**
   * A message to trigger the loopback adapter in single step mode.
   */
  public static final String TRIGGER_SINGLE_STEP = "tcs:virtualVehicle:triggerSingleStep";

  /**
   * Prevents instantiation.
   */
  private LoopbackCommAdapterMessages() {
  }
}
