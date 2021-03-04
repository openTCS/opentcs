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
   * Notifies the dispatcher that it should start the dispatching process.
   */
  void dispatch();

  /**
   * Notifies the dispatcher that the given vehicle may now be dispatched.
   *
   * @param vehicle The dispatchable vehicle.
   * @deprecated Use {@link #dispatch()} instead.
   */
  @Deprecated
  default void dispatch(@Nonnull Vehicle vehicle) {
    dispatch();
  }

  /**
   * Notifies the dispatcher that the given transport order may now be
   * dispatched.
   *
   * @param order The dispatchable order.
   * @deprecated Use {@link #dispatch()} instead.
   */
  @Deprecated
  default void dispatch(@Nonnull TransportOrder order) {
    dispatch();
  }

  /**
   * Notifies the dispatcher that the given transport order is to be withdrawn/aborted.
   *
   * @param order The transport order to be withdrawn/aborted.
   * @param immediateAbort Whether the order should be aborted immediately instead of withdrawn.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default void withdrawOrder(@Nonnull TransportOrder order, boolean immediateAbort) {
    withdrawOrder(order, immediateAbort, false);
  }

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
   * @deprecated Use {@link #withdrawOrder(org.opentcs.data.order.TransportOrder, boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void withdrawOrder(@Nonnull TransportOrder order, boolean immediateAbort, boolean disableVehicle);

  /**
   * Notifies the dispatcher that any order a given vehicle might be processing is to be withdrawn.
   *
   * @param vehicle The vehicle whose order is withdrawn.
   * @param immediateAbort Whether the vehicle's order should be aborted immediately instead of
   * withdrawn.
   */
  default void withdrawOrder(@Nonnull Vehicle vehicle, boolean immediateAbort) {
    withdrawOrder(vehicle, immediateAbort, false);
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
   * @deprecated Use {@link #withdrawOrder(org.opentcs.data.model.Vehicle, boolean)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void withdrawOrder(@Nonnull Vehicle vehicle, boolean immediateAbort, boolean disableVehicle);

  /**
   * Notifies the dispatcher that the given vehicle should be released, aborting its transport
   * order, resetting its position and freeing any allocated resources.
   *
   * @param vehicle The vehicle.
   * @deprecated To achieve the same, withdraw a vehicle's order and set its integration level to
   * {@link Vehicle.IntegrationLevel#TO_BE_IGNORED}.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void releaseVehicle(@Nonnull Vehicle vehicle);

  /**
   * Notifies the dispatcher of changes in the topology.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default void topologyChanged() {
  }
  
  /**
   * Returns a human readable text describing this dispatcher's internal state.
   *
   * @return A human readable text describing this dispatcher's internal state.
   * @deprecated Does not serve any real purpose and will be removed.
   */
  @Nonnull
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  default String getInfo() {
    return "";
  }
}
