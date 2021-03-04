/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_DISPATCHER_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_KERNEL_CLIENT_PORTAL;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_NOTIFICATION_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_PLANT_MODEL_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_ROUTER_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_SCHEDULER_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_TRANSPORT_ORDER_SERVICE;
import static org.opentcs.access.rmi.services.RegistrationName.REMOTE_VEHICLE_SERVICE;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.NotificationService;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.components.kernel.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for the {@link KernelServicePortal}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RemoteKernelServicePortalProxy
    extends AbstractRemoteServiceProxy<RemoteKernelServicePortal>
    implements KernelServicePortal,
               ServiceListener {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RemoteKernelServicePortalProxy.class);
  /**
   * The user name used with the remote portal.
   */
  private final String userName;
  /**
   * The password used with the remote portal.
   */
  private final String password;
  /**
   * Provides socket factories used for RMI.
   */
  private final SocketFactoryProvider socketFactoryProvider;
  /**
   * The event filter to be applied to events on the server side (before polling).
   */
  private final Predicate<Object> eventFilter;
  /**
   * The plant model service.
   */
  private final RemotePlantModelServiceProxy plantModelService
      = new RemotePlantModelServiceProxy();
  /**
   * The transport order service.
   */
  private final RemoteTransportOrderServiceProxy transportOrderService
      = new RemoteTransportOrderServiceProxy();
  /**
   * The vehicle service.
   */
  private final RemoteVehicleServiceProxy vehicleService = new RemoteVehicleServiceProxy();
  /**
   * The notification service.
   */
  private final RemoteNotificationServiceProxy notificationService
      = new RemoteNotificationServiceProxy();
  /**
   * The dispatcher service.
   */
  private final RemoteDispatcherServiceProxy dispatcherService
      = new RemoteDispatcherServiceProxy();
  /**
   * The router service.
   */
  private final RemoteRouterServiceProxy routerService = new RemoteRouterServiceProxy();
  /**
   * The scheduler service.
   */
  private final RemoteSchedulerServiceProxy schedulerService
      = new RemoteSchedulerServiceProxy();

  /**
   * Creates a new instance.
   *
   * @param userName The user name used with the remote portal.
   * @param password The password used with the remote portal.
   * @param socketFactoryProvider Provides socket factories used for RMI.
   * @param eventFilter The event filter to be applied to events on the server side.
   */
  public RemoteKernelServicePortalProxy(
      @Nonnull String userName,
      @Nonnull String password,
      @Nonnull SocketFactoryProvider socketFactoryProvider,
      @Nonnull Predicate<Object> eventFilter) {
    this.userName = requireNonNull(userName, "userName");
    this.password = requireNonNull(password, "password");
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    this.eventFilter = requireNonNull(eventFilter, "eventFilter");
  }

  @Override
  public ServiceListener getServiceListener() {
    return this;
  }

  @Override
  public void onServiceUnavailable() {
    resetServiceLogins();
  }

  @Override
  public void login(@Nonnull String hostName, int port)
      throws CredentialsException, ServiceUnavailableException {
    requireNonNull(hostName, "hostName");

    if (isLoggedIn()) {
      LOG.warn("Already logged in, doing nothing.");
      return;
    }

    try {
      // Look up the remote portal with the RMI registry.
      Registry registry = LocateRegistry.getRegistry(hostName,
                                                     port,
                                                     socketFactoryProvider.getClientSocketFactory());

      setRemoteService((RemoteKernelServicePortal) registry.lookup(REMOTE_KERNEL_CLIENT_PORTAL));
      // Login and save the client ID.
      setClientId(getRemoteService().login(userName, password, eventFilter));
      // Get notified when a service call on us fails.
      setServiceListener(this);

      // Look up the remote services with the RMI registry and update the other service logins.
      updateServiceLogins(registry);
    }
    catch (RemoteException | NotBoundException exc) {
      resetServiceLogins();
      throw new ServiceUnavailableException("Exception logging in with remote kernel client portal",
                                            exc.getCause());
    }
  }

  @Override
  public void logout() {
    if (!isLoggedIn()) {
      LOG.warn("Not logged in, doing nothing.");
      return;
    }

    try {
      getRemoteService().logout(getClientId());
    }
    catch (RemoteException ex) {
      throw new ServiceUnavailableException("Remote kernel client portal unavailable", ex);
    }

    resetServiceLogins();
  }

  @Override
  public Kernel.State getState()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().getState(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public List<Object> fetchEvents(long timeout)
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().fetchEvents(getClientId(), timeout);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  @Nonnull
  public PlantModelService getPlantModelService() {
    return plantModelService;
  }

  @Override
  @Nonnull
  public TransportOrderService getTransportOrderService() {
    return transportOrderService;
  }

  @Override
  @Nonnull
  public VehicleService getVehicleService() {
    return vehicleService;
  }

  @Override
  @Nonnull
  public NotificationService getNotificationService() {
    return notificationService;
  }

  @Override
  @Nonnull
  public DispatcherService getDispatcherService() {
    return dispatcherService;
  }

  @Override
  @Nonnull
  public RouterService getRouterService() {
    return routerService;
  }

  @Override
  @Nonnull
  public SchedulerService getSchedulerService() {
    return schedulerService;
  }

  private void updateServiceLogins(Registry registry)
      throws RemoteException, NotBoundException {
    plantModelService
        .setClientId(getClientId())
        .setRemoteService((RemotePlantModelService) registry.lookup(REMOTE_PLANT_MODEL_SERVICE))
        .setServiceListener(this);

    transportOrderService
        .setClientId(getClientId())
        .setRemoteService((RemoteTransportOrderService) registry.lookup(REMOTE_TRANSPORT_ORDER_SERVICE))
        .setServiceListener(this);

    vehicleService
        .setClientId(getClientId())
        .setRemoteService((RemoteVehicleService) registry.lookup(REMOTE_VEHICLE_SERVICE))
        .setServiceListener(this);

    notificationService
        .setClientId(getClientId())
        .setRemoteService((RemoteNotificationService) registry.lookup(REMOTE_NOTIFICATION_SERVICE))
        .setServiceListener(this);

    dispatcherService
        .setClientId(getClientId())
        .setRemoteService((RemoteDispatcherService) registry.lookup(REMOTE_DISPATCHER_SERVICE))
        .setServiceListener(this);

    routerService
        .setClientId(getClientId())
        .setRemoteService((RemoteRouterService) registry.lookup(REMOTE_ROUTER_SERVICE))
        .setServiceListener(this);

    schedulerService
        .setClientId(getClientId())
        .setRemoteService((RemoteSchedulerService) registry.lookup(REMOTE_SCHEDULER_SERVICE))
        .setServiceListener(this);
  }

  private void resetServiceLogins() {
    this.setClientId(null).setRemoteService(null).setServiceListener(null);
    plantModelService.setClientId(null).setRemoteService(null).setServiceListener(null);
    transportOrderService.setClientId(null).setRemoteService(null).setServiceListener(null);
    vehicleService.setClientId(null).setRemoteService(null).setServiceListener(null);
    notificationService.setClientId(null).setRemoteService(null).setServiceListener(null);
    dispatcherService.setClientId(null).setRemoteService(null).setServiceListener(null);
    routerService.setClientId(null).setRemoteService(null).setServiceListener(null);
    schedulerService.setClientId(null).setRemoteService(null).setServiceListener(null);
  }
}
