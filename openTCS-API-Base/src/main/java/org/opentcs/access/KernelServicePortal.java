/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;

/**
 * Provides clients access to kernel services.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface KernelServicePortal {

  /**
   * Logs in with/establishes a connection to the remote kernel service portal.
   *
   * @param hostName The host on which the remote portal is running.
   * @param port The port at which we can reach the remote RMI registry.
   * @throws KernelRuntimeException If there was a problem logging in with the remote portal.
   */
  void login(@Nonnull String hostName, int port)
      throws KernelRuntimeException;

  /**
   * Logs out from/clears the connection to the remote kernel service portal.
   *
   * @throws KernelRuntimeException If there was a problem logging out from the remote portal, i.e.
   * it is no longer available.
   */
  void logout()
      throws KernelRuntimeException;

  /**
   * Returns the current state of the kernel.
   *
   * @return The current state of the kernel.
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  Kernel.State getState()
      throws KernelRuntimeException;

  /**
   * Fetches events buffered for the given client.
   *
   * @param timeout A timeout (in ms) for which to wait for events to arrive.
   * @return A list of events (in the order they arrived).
   * @throws KernelRuntimeException In case there is an exception executing this method.
   */
  List<Object> fetchEvents(long timeout)
      throws KernelRuntimeException;

  /**
   * Returns the service a client can use to access methods regarding the plant model.
   *
   * @return The service a client can use to access methods regarding the plant model.
   */
  @Nonnull
  PlantModelService getPlantModelService();

  /**
   * Returns the service a client can use to access methods regarding transport orders and order
   * sequences.
   *
   * @return The service a client can use to access methods regarding transport orders and order
   * sequences.
   */
  @Nonnull
  TransportOrderService getTransportOrderService();

  /**
   * Returns the service a client can use to access methods regarding vehicles.
   *
   * @return The service a client can use to access methods regarding vehicles.
   */
  @Nonnull
  VehicleService getVehicleService();

  /**
   * Returns the service a client can use to access methods regarding user notifications.
   *
   * @return The service a client can use to access methods regarding user notifications.
   */
  @Nonnull
  NotificationService getNotificationService();

  /**
   * Returns the service a client can use to access methods regarding the dispatcher.
   *
   * @return The service a client can use to access methods regarding the dispatcher.
   */
  @Nonnull
  DispatcherService getDispatcherService();

  /**
   * Returns the service a client can use to access methods regarding the router.
   *
   * @return The service a client can use to access methods regarding the router.
   */
  @Nonnull
  RouterService getRouterService();

  /**
   * Returns the service a client can use to access methods regarding the scheduler.
   *
   * @return The service a client can use to access methods regarding the scheduler.
   */
  @Nonnull
  SchedulerService getSchedulerService();
}
