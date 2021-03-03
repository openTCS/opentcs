/*
 * openTCS copyright information:
 * Copyright (c) 2005 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.algorithms;

import org.opentcs.data.model.Vehicle;
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
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface Dispatcher {

  /**
   * Notifies the dispatcher that the given vehicle may now be dispatched.
   *
   * @param vehicle The dispatchable vehicle.
   */
  void dispatch(Vehicle vehicle);

  /**
   * Notifies the dispatcher that the given transport order may now be
   * dispatched.
   *
   * @param order The dispatchable order.
   */
  void dispatch(TransportOrder order);

  /**
   * Notifies the dispatcher that the given transport order is to be
   * withdrawn/aborted and any vehicle that might be processing it to be
   * stopped.
   *
   * @param order The transport order to be withdrawn/aborted.
   * @param immediateAbort Whether the order should be aborted immediately
   * instead of withdrawn.
   * @param disableVehicle Whether to set the processing vehicle's processing
   * state to UNAVAILABLE after withdrawing the order to prevent the vehicle
   * being dispatched again.
   */
  default void withdrawOrder(TransportOrder order,
                             boolean immediateAbort,
                             boolean disableVehicle) {
    // This empty default implementation will be reduced to a plain interface
    // declaration with release 4.0.0.
  }

  /**
   * Notifies the dispatcher that any order a given vehicle might be processing
   * is to be withdrawn and the vehicle stopped.
   *
   * @param vehicle The vehicle whose order is withdrawn.
   * @param immediateAbort Whether the vehicle's order should be aborted
   * immediately instead of withdrawn.
   * @param disableVehicle Whether to set the vehicle's processing state to
   * UNAVAILABLE after withdrawing the order to prevent the vehicle being
   * dispatched for now.
   */
  default void withdrawOrder(Vehicle vehicle,
                             boolean immediateAbort,
                             boolean disableVehicle) {
    withdrawOrder(vehicle, disableVehicle);
  }

  /**
   * Notifies the dispatcher that any order a given vehicle might be processing
   * is to be withdrawn and the vehicle stopped.
   *
   * @param vehicle The vehicle whose order is withdrawn.
   * @param disableVehicle Whether to set the vehicle's processing state to
   * UNAVAILABLE after withdrawing the order to prevent the vehicle being
   * dispatched again.
   * @deprecated Scheduled for removal with release 4.0.0. Call/implement
   * {@link #withdrawOrder(org.opentcs.data.model.Vehicle, boolean, boolean)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "4.0.0")
  void withdrawOrder(Vehicle vehicle, boolean disableVehicle);

  /**
   * Initializes the dispatcher before first use.
   */
  void initialize();

  /**
   * Terminates the dispatcher and frees all resources it might have allocated.
   */
  void terminate();

  /**
   * Returns a human readable text describing this router's internal state.
   *
   * @return A human readable text describing this router's internal state.
   */
  String getInfo();
}
