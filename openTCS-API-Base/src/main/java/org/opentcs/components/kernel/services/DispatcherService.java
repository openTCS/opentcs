/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.services;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.data.order.TransportOrder.State;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods concerning the {@link Dispatcher}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface DispatcherService {

  /**
   * Explicitly trigger the dispatching process.
   *
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  void dispatch()
      throws KernelRuntimeException;

  /**
   * Reset a vehicle's position, implicitly aborting any transport order it is currently processing,
   * disabling it for transport order disposition and freeing any allocated resources.
   *
   * @param ref A reference to the vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Withdraw the vehicle's transport order and set its integration level to
   * {@link Vehicle.IntegrationLevel#TO_BE_IGNORED} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void releaseVehicle(TCSObjectReference<Vehicle> ref)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Withdraw any order that a vehicle might be processing.
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
   * Withdraw any order that a vehicle might be processing, set its state to {@link State#FAILED}
   * and stop the vehicle.
   *
   * @param ref A reference to the vehicle to be modified.
   * @param immediateAbort If {@code false}, this method once will initiate the withdrawal, leaving
   * the transport order assigned to the vehicle until it has finished the movements that it has
   * already been ordered to execute. The transport order's state will change to
   * {@link State#WITHDRAWN}. If {@code true}, the dispatcher will withdraw the order from the
   * vehicle without further waiting.
   * @param disableVehicle Whether to set the processing state of the vehicle currently processing
   * the transport order to {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced vehicle does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #withdrawByVehicle(org.opentcs.data.TCSObjectReference, boolean)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void withdrawByVehicle(TCSObjectReference<Vehicle> ref,
                         boolean immediateAbort,
                         boolean disableVehicle)
      throws ObjectUnknownException, KernelRuntimeException;

  /**
   * Withdraw the referenced order.
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
   * Withdraw the referenced order, set its state to {@link State#FAILED} and stop the vehicle that
   * might be processing it.
   *
   * @param ref A reference to the transport order to be withdrawn.
   * @param immediateAbort If {@code false}, this method once will initiate the withdrawal, leaving
   * the transport order assigned to the vehicle until it has finished the movements that it has
   * already been ordered to execute. The transport order's state will change to
   * {@link State#WITHDRAWN}. If {@code true}, the dispatcher will withdraw the order from the
   * vehicle without further waiting.
   * @param disableVehicle Whether to set the processing state of the vehicle currently processing
   * the transport order to {@link org.opentcs.data.model.Vehicle.ProcState#UNAVAILABLE} to prevent
   * immediate redispatching of the vehicle.
   * @throws ObjectUnknownException If the referenced transport order does not exist.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   * @deprecated Use {@link #withdrawByTransportOrder(org.opentcs.data.TCSObjectReference, boolean)}
   * instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  void withdrawByTransportOrder(TCSObjectReference<TransportOrder> ref,
                                boolean immediateAbort,
                                boolean disableVehicle)
      throws ObjectUnknownException, KernelRuntimeException;
}
