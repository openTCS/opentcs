// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

/**
 * Constants for commonly used error types in state messages.
 */
public abstract class ErrorTypes {
  /**
   * Indicates that the vehicle rejects the received order because it was malformed.
   */
  public static final String VALIDATION_ERROR = "validationError";
  /**
   * Indicates that the vehicle rejects the received order because it is not within the deviation
   * range of the order's first node.
   */
  public static final String NO_ROUTE_ERROR = "noRouteError";
  /**
   * Indicates that the vehicle rejects the received order because it contained actions the vehicle
   * cannot perform, or fields that it cannot use (e.g. trajectory).
   */
  public static final String ORDER_ERROR = "orderError";
  /**
   * Indicates that the vehicle rejects the received order update because it was invalid.
   */
  public static final String ORDER_UPDATE_ERROR = "orderUpdateError";
}
