/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

/**
 * Defines constants for basic history event codes related to transport orders and documents how the
 * respective supplementary information is to be interpreted.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface TransportOrderHistoryCodes {

  /**
   * An event code indicating a transport order has been created.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_CREATED = "tcsHistory:orderCreated";
  /**
   * An event code indicating dispatching of a transport order to a vehicle has been deferred.
   * <p>
   * The history entry's supplement contains a list of reasons for the deferral.
   * </p>
   */
  String ORDER_DISPATCHING_DEFERRED = "tcsHistory:orderDispatchingDeferred";
  /**
   * An event code indicating dispatching of a transport order to a vehicle has been resumed.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_DISPATCHING_RESUMED = "tcsHistory:orderDispatchingResumed";
  /**
   * An event code indicating a transport order was assigned to a vehicle.
   * <p>
   * The history entry's supplement contains the name of the vehicle the transport order was
   * assigned to.
   * </p>
   */
  String ORDER_ASSIGNED_TO_VEHICLE = "tcsHistory:orderAssignedToVehicle";
  /**
   * An event code indicating a transport order was reserved for a vehicle.
   * <p>
   * The history entry's supplement contains the name of the vehicle the transport order was
   * reserved for.
   * </p>
   */
  String ORDER_RESERVED_FOR_VEHICLE = "tcsHistory:orderReservedForVehicle";
  /**
   * An event code indicating a transport order's processing vehicle changed.
   * <p>
   * The history entry's supplement contains the name of the new processing vehicle, or the empty
   * string, if the processing vehicle was unset.
   * </p>
   */
  String ORDER_PROCESSING_VEHICLE_CHANGED = "tcsHistory:orderProcVehicleChanged";
  /**
   * An event code indicating a transport order was marked as being in a final state.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_REACHED_FINAL_STATE = "tcsHistory:orderReachedFinalState";
  /**
   * An event code indicating one of a transport order's drive orders has been finished.
   * <p>
   * The history entry's supplement is empty.
   * </p>
   */
  String ORDER_DRIVE_ORDER_FINISHED = "tcsHistory:orderFinishedDriveOrder";
}
