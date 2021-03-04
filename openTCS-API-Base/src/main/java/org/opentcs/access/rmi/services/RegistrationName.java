/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

/**
 * Defines the names used for binding the remote services in the RMI registry.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RegistrationName {

  /**
   * The name the {@link RemoteKernelServicePortal} registers itself with a RMI registry.
   */
  String REMOTE_KERNEL_CLIENT_PORTAL = RemoteKernelServicePortal.class.getCanonicalName();
  /**
   * The name the {@link RemotePlantModelService} registers itself with a RMI registry.
   */
  String REMOTE_PLANT_MODEL_SERVICE = RemotePlantModelService.class.getCanonicalName();
  /**
   * The name the {@link RemoteTransportOrderService} registers itself with a RMI registry.
   */
  String REMOTE_TRANSPORT_ORDER_SERVICE = RemoteTransportOrderService.class.getCanonicalName();
  /**
   * The name the {@link RemoteVehicleService} registers itself with a RMI registry.
   */
  String REMOTE_VEHICLE_SERVICE = RemoteVehicleService.class.getCanonicalName();
  /**
   * The name the {@link RemoteNotificationService} registers itself with a RMI registry.
   */
  String REMOTE_NOTIFICATION_SERVICE = RemoteNotificationService.class.getCanonicalName();
  /**
   * The name the {@link RemoteRouterService} registers itself with a RMI registry.
   */
  String REMOTE_ROUTER_SERVICE = RemoteRouterService.class.getCanonicalName();
  /**
   * The name the {@link RemoteDispatcherService} registers itself with a RMI registry.
   */
  String REMOTE_DISPATCHER_SERVICE = RemoteDispatcherService.class.getCanonicalName();
  /**
   * The name the {@link RemoteSchedulerService} registers itself with a RMI registry.
   */
  String REMOTE_SCHEDULER_SERVICE = RemoteSchedulerService.class.getCanonicalName();
}
