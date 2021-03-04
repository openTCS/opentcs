/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.statistics;

/**
 * Defines labels for events relevant to statistics.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public enum StatisticsEvent {

  /**
   * Indicates a transport order's state has changed to ACTIVE.
   */
  ORDER_ACTIVATED,
  /**
   * Indicates a transport order has been assigned to a vehicle.
   */
  ORDER_ASSIGNED,
  /**
   * Indicates a transport order has been finished successfully.
   */
  ORDER_FINISHED_SUCC,
  /**
   * Indicates a transport order has been finished unsuccessfully.
   */
  ORDER_FINISHED_FAIL,
  /**
   * Indicates an order was processed after its deadline.
   */
  ORDER_CROSSED_DEADLINE,
  /**
   * Indicates a vehicle has started processing an order.
   */
  VEHICLE_STARTS_PROCESSING,
  /**
   * Indicates a vehicle has finished processing an order.
   */
  VEHICLE_STOPS_PROCESSING,
  /**
   * Indicates a vehicle has started recharging its energy source.
   */
  VEHICLE_STARTS_CHARGING,
  /**
   * Indicates a vehicle has stopped recharging its energy source.
   */
  VEHICLE_STOPS_CHARGING,
  /**
   * Indicates a vehicle has started waiting for a resource/command.
   */
  VEHICLE_STARTS_WAITING,
  /**
   * Indicates a vehicle has stopped waiting for a resource/command.
   */
  VEHICLE_STOPS_WAITING,
  /**
   * Indicates a point has been occupied by a vehicle.
   */
  POINT_OCCUPIED,
  /**
   * Indicates a point has no longer been occupied by a vehicle.
   */
  POINT_FREED
}
