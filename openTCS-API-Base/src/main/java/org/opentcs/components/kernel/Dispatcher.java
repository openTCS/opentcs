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
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

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
public interface Dispatcher
    extends Lifecycle {

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
   * Notifies the dispatcher that the given vehicle may now be dispatched.
   *
   * @param vehicle The dispatchable vehicle.
   */
  void dispatch(@Nonnull Vehicle vehicle);

  /**
   * Notifies the dispatcher that the given transport order may now be
   * dispatched.
   *
   * @param order The dispatchable order.
   */
  void dispatch(@Nonnull TransportOrder order);

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
  void withdrawOrder(@Nonnull TransportOrder order, boolean immediateAbort, boolean disableVehicle);

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
  void withdrawOrder(@Nonnull Vehicle vehicle,
                     boolean immediateAbort,
                     boolean disableVehicle);

  /**
   * Notifies the dispatcher that the given vehicle should be released, aborting its transport
   * order, resetting its position and freeing any allocated resources.
   *
   * @param vehicle The vehicle.
   */
  void releaseVehicle(@Nonnull Vehicle vehicle);

  /**
   * Returns a human readable text describing this router's internal state.
   *
   * @return A human readable text describing this router's internal state.
   */
  @Nonnull
  String getInfo();
}
