// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemoteRouterService;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemoteRouterService} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * {@link RegistrationName#REMOTE_ROUTER_SERVICE}.
 * </p>
 */
public class StandardRemoteRouterService
    extends
      KernelRemoteService
    implements
      RemoteRouterService {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemoteRouterService.class);
  /**
   * The scheduler service to invoke methods on.
   */
  private final RouterService routerService;
  /**
   * The user manager.
   */
  private final UserManager userManager;
  /**
   * Provides configuration data.
   */
  private final RmiKernelInterfaceConfiguration configuration;
  /**
   * Provides socket factories used for RMI.
   */
  private final SocketFactoryProvider socketFactoryProvider;
  /**
   * Provides the registry with which this remote service registers.
   */
  private final RegistryProvider registryProvider;
  /**
   * Executes tasks modifying kernel data.
   */
  private final ExecutorService kernelExecutor;
  /**
   * The registry with which this remote service registers.
   */
  private Registry rmiRegistry;
  /**
   * Whether this remote service is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param routerService The router service.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider The provider for the registry with which this remote service registers.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public StandardRemoteRouterService(
      RouterService routerService,
      UserManager userManager,
      RmiKernelInterfaceConfiguration configuration,
      SocketFactoryProvider socketFactoryProvider,
      RegistryProvider registryProvider,
      @KernelExecutor
      ExecutorService kernelExecutor
  ) {
    this.routerService = requireNonNull(routerService, "routerService");
    this.userManager = requireNonNull(userManager, "userManager");
    this.configuration = requireNonNull(configuration, "configuration");
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    this.registryProvider = requireNonNull(registryProvider, "registryProvider");
    this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    rmiRegistry = registryProvider.get();

    // Export this instance via RMI.
    try {
      LOG.debug("Exporting proxy...");
      UnicastRemoteObject.exportObject(
          this,
          configuration.remoteRouterServicePort(),
          socketFactoryProvider.getClientSocketFactory(),
          socketFactoryProvider.getServerSocketFactory()
      );
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(RegistrationName.REMOTE_ROUTER_SERVICE, this);
    }
    catch (RemoteException exc) {
      LOG.error("Could not export or bind with RMI registry", exc);
      return;
    }

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    try {
      LOG.debug("Unbinding from RMI registry...");
      rmiRegistry.unbind(RegistrationName.REMOTE_ROUTER_SERVICE);
      LOG.debug("Unexporting RMI interface...");
      UnicastRemoteObject.unexportObject(this, true);
    }
    catch (RemoteException | NotBoundException exc) {
      LOG.warn("Exception shutting down RMI interface", exc);
    }

    initialized = false;
  }

  @Override
  public void updateRoutingTopology(ClientID clientId, Set<TCSObjectReference<Path>> refs)
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_MODEL);

    try {
      kernelExecutor.submit(() -> routerService.updateRoutingTopology(refs)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public Map<TCSObjectReference<Point>, Set<Route>> computeRoutes(
      ClientID clientId,
      TCSObjectReference<Vehicle> vehicleRef,
      TCSObjectReference<Point> sourcePointRef,
      Set<TCSObjectReference<Point>> destinationPointRefs,
      Set<TCSResourceReference<?>> resourcesToAvoid,
      int maxRoutesPerDestinationPoint
  )
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_MODEL);

    try {
      return kernelExecutor.submit(
          () -> routerService.computeRoutes(
              vehicleRef,
              sourcePointRef,
              destinationPointRefs,
              resourcesToAvoid,
              maxRoutesPerDestinationPoint
          )
      )
          .get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }
}
