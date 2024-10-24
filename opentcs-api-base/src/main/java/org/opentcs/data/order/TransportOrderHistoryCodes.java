// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

/**
 * Defines constants for basic history event codes related to transport orders and documents how the
 * respective supplementary information is to be interpreted.
 */
public interface TransportOrderHistoryCodes {

  /**
   * An event code indicating a transport order has been created.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_CREATED = "tcs:history:orderCreated";
  /**
   * An event code indicating dispatching of a transport order to a vehicle has been deferred.
   * <p>
   * The history entry's supplement contains a list of reasons for the deferral.
   * </p>
   */
  String ORDER_DISPATCHING_DEFERRED = "tcs:history:orderDispatchingDeferred";
  /**
   * An event code indicating dispatching of a transport order to a vehicle has been resumed.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_DISPATCHING_RESUMED = "tcs:history:orderDispatchingResumed";
  /**
   * An event code indicating a transport order was assigned to a vehicle.
   * <p>
   * The history entry's supplement contains the name of the vehicle the transport order was
   * assigned to.
   * </p>
   */
  String ORDER_ASSIGNED_TO_VEHICLE = "tcs:history:orderAssignedToVehicle";
  /**
   * An event code indicating a transport order was reserved for a vehicle.
   * <p>
   * The history entry's supplement contains the name of the vehicle the transport order was
   * reserved for.
   * </p>
   */
  String ORDER_RESERVED_FOR_VEHICLE = "tcs:history:orderReservedForVehicle";
  /**
   * An event code indicating a transport order's processing vehicle changed.
   * <p>
   * The history entry's supplement contains the name of the new processing vehicle, or the empty
   * string, if the processing vehicle was unset.
   * </p>
   */
  String ORDER_PROCESSING_VEHICLE_CHANGED = "tcs:history:orderProcVehicleChanged";
  /**
   * An event code indicating a transport order was marked as being in a final state.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_REACHED_FINAL_STATE = "tcs:history:orderReachedFinalState";
  /**
   * An event code indicating one of a transport order's drive orders has been finished.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_DRIVE_ORDER_FINISHED = "tcs:history:orderFinishedDriveOrder";
}
