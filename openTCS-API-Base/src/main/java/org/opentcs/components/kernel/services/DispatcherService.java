/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import javax.annotation.Nonnull;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.components.kernel.dipatching.TransportOrderAssignmentException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.order.TransportOrder.State;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the {@link Dispatcher}.
 */
public interface DispatcherService {

  /**
   * Explicitly trigger the dispatching process.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void dispatch()
      throws KernelRuntimeException;

  /**
   * Withdraw any order that a vehicle might be processing.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the vehicle to be modified.
   * @param immediateAbort If {@code false}, this method once will initiate the withdrawal, leaving
   * the transport order assigned to the vehicle until it has finished the movements that it has
   * already been ordered to execute. The transport order's state will change to
   * {@link State#WITHDRAWN}. If {@code true}, the dispatcher will withdraw the order from the
   * vehicle without further waiting.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void withdrawByVehicle(TCSObjectReference<Vehicle> ref, boolean immediateAbort)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Withdraw the referenced order.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref A reference to the transport order to be withdrawn.
   * @param immediateAbort If {@code false}, this method once will initiate the withdrawal, leaving
   * the transport order assigned to the vehicle until it has finished the movements that it has
   * already been ordered to execute. The transport order's state will change to
   * {@link State#WITHDRAWN}. If {@code true}, the dispatcher will withdraw the order from the
   * vehicle without further waiting.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref, boolean immediateAbort)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Explicitly trigger a rerouting for the given vehicles.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref The vehicle to be rerouted.
   * @param reroutingType The type of the requested rerouting.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void reroute(@Nonnull TCSObjectReference<Vehicle> ref,
                       @Nonnull ReroutingType reroutingType)
      throws ObjectUnknownException, KernelRuntimeException {
  }

  /**
   * Explicitly trigger a rerouting for all vehicles.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param reroutingType The type of rerouting.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void rerouteAll(@Nonnull ReroutingType reroutingType) {
  }

  /**
   * Assign the referenced transport order (to its intended vehicle) <em>now</em>.
   * <p>
   * If called within the kernel application, this method is supposed to be called only on the
   * kernel executor thread.
   * </p>
   *
   * @param ref The transport order to be assigned.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   * @throws TransportOrderAssignmentException If the given transport order could not be assigned
   * to its intended vehicle.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  @ScheduledApiChange(when = "6.0", details = "Default implementation will be removed.")
  default void assignNow(TCSObjectReference<TransportOrder> ref)
      throws ObjectUnknownException, TransportOrderAssignmentException, KernelRuntimeException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
