/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * This interface declares the methods a dispatcher module for the openTCS
 * kernel must implement.
 * <p>
 * A dispatcher manages the distribution of transport orders among the vehicles
 * in a system. It is basically event-driven, where an event can be a new
 * transport order being introduced into the system or a vehicle becoming
 * available for processing existing orders.
 * </p>
 */
public interface Dispatcher
    extends Lifecycle {

  /**
   * The key of a parking position property defining its priority.
   * <p>
   * Whether and in what way this is respected for assigning a parking position to a vehicle is
   * implementation-specific.
   * </p>
   */
  String PROPKEY_PARKING_POSITION_PRIORITY = "tcs:parkingPositionPriority";
  /**
   * The key of a vehicle property defining the name of the vehicle's assigned parking position.
   * <p>
   * Whether and in what way this is respected for selecting a parking position is
   * implementation-specific.
   * </p>
   */
  String PROPKEY_ASSIGNED_PARKING_POSITION = "tcs:assignedParkingPosition";
  /**
   * The key of a vehicle property defining the name of the vehicle's preferred parking position.
   * <p>
   * Whether and in what way this is respected for selecting a parking position is
   * implementation-specific.
   * </p>
   */
  String PROPKEY_PREFERRED_PARKING_POSITION = "tcs:preferredParkingPosition";
  /**
   * The key of a vehicle property defining the name of the vehicle's assigned recharge location.
   * <p>
   * Whether and in what way this is respected for selecting a recharge location is
   * implementation-specific.
   * </p>
   */
  String PROPKEY_ASSIGNED_RECHARGE_LOCATION = "tcs:assignedRechargeLocation";
  /**
   * The key of a vehicle property defining the name of the vehicle's preferred recharge location.
   * <p>
   * Whether and in what way this is respected for selecting a recharge location is
   * implementation-specific.
   * </p>
   */
  String PROPKEY_PREFERRED_RECHARGE_LOCATION = "tcs:preferredRechargeLocation";

  /**
   * Notifies the dispatcher that it should start the dispatching process.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   */
  void dispatch();

  /**
   * Notifies the dispatcher that the given transport order is to be withdrawn/aborted.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param order The transport order to be withdrawn/aborted.
   * @param immediateAbort Whether the order should be aborted immediately instead of withdrawn.
   */
  void withdrawOrder(@Nonnull TransportOrder order, boolean immediateAbort);

  /**
   * Notifies the dispatcher that any order a given vehicle might be processing is to be withdrawn.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param vehicle The vehicle whose order is withdrawn.
   * @param immediateAbort Whether the vehicle's order should be aborted immediately instead of
   * withdrawn.
   */
  void withdrawOrder(@Nonnull Vehicle vehicle, boolean immediateAbort);

  /**
   * Notifies the dispatcher of changes in the topology.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @deprecated Use {@link #rerouteAll(org.opentcs.data.order.ReroutingType)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  default void topologyChanged() {
  }

  /**
   * Notifies the dispatcher of a request to reroute the given vehicle considering the given
   * rerouting type.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param vehicle The vehicle to be rerouted.
   * @param reroutingType The type of the requested rerouting.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void reroute(@Nonnull Vehicle vehicle, @Nonnull ReroutingType reroutingType) {
  }

  /**
   * Notifies the dispatcher to reroute all vehicles considering the given rerouting type.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param reroutingType The type of the requested rerouting.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void rerouteAll(@Nonnull ReroutingType reroutingType) {
  }

  /**
   * Notifies the dispatcher that it should assign the given transport order (to its intended
   * vehicle) <em>now</em>.
   * <p>
   * This method is supposed to be called only from the kernel executor thread.
   * </p>
   *
   * @param transportOrder The transport order to be assigned.
   * @throws TransportOrderAssignmentException If the given transport order could not be assigned
   * to its intended vehicle.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void assignNow(@Nonnull TransportOrder transportOrder)
      throws TransportOrderAssignmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
