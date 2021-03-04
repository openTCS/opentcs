/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemoteVehicleService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.kernel.util.RegistryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemoteVehicleService} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * declared as {@link RemoteVehicleService#getRegistrationName()}.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardRemoteVehicleService
    extends StandardRemoteTCSObjectService
    implements RemoteVehicleService {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemoteVehicleService.class);
  /**
   * The vehicle service to invoke methods on.
   */
  private final VehicleService vehicleService;
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
   * @param vehicleService The vehicle service.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider The provider for the registry with which this remote service registers.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public StandardRemoteVehicleService(VehicleService vehicleService,
                                      UserManager userManager,
                                      RmiKernelInterfaceConfiguration configuration,
                                      SocketFactoryProvider socketFactoryProvider,
                                      RegistryProvider registryProvider,
                                      @KernelExecutor ExecutorService kernelExecutor) {
    super(vehicleService, userManager, kernelExecutor);
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
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
      UnicastRemoteObject.exportObject(this,
                                       configuration.remoteVehicleServicePort(),
                                       socketFactoryProvider.getClientSocketFactory(),
                                       socketFactoryProvider.getServerSocketFactory());
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(RegistrationName.REMOTE_VEHICLE_SERVICE, this);
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
      rmiRegistry.unbind(RegistrationName.REMOTE_VEHICLE_SERVICE);
      LOG.debug("Unexporting RMI interface...");
      UnicastRemoteObject.unexportObject(this, true);
    }
    catch (RemoteException | NotBoundException exc) {
      LOG.warn("Exception shutting down RMI interface", exc);
    }

    initialized = false;
  }

  @Override
  public void attachCommAdapter(ClientID clientId,
                                TCSObjectReference<Vehicle> ref,
                                VehicleCommAdapterDescription description) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.attachCommAdapter(ref, description)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void disableCommAdapter(ClientID clientId, TCSObjectReference<Vehicle> ref) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.disableCommAdapter(ref)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void enableCommAdapter(ClientID clientId, TCSObjectReference<Vehicle> ref) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.enableCommAdapter(ref)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public AttachmentInformation fetchAttachmentInformation(ClientID clientId,
                                                          TCSObjectReference<Vehicle> ref) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return vehicleService.fetchAttachmentInformation(ref);
  }

  @Override
  public VehicleProcessModelTO fetchProcessModel(ClientID clientId,
                                                 TCSObjectReference<Vehicle> ref) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return vehicleService.fetchProcessModel(ref);
  }

  @Override
  public void sendCommAdapterCommand(ClientID clientId,
                                     TCSObjectReference<Vehicle> ref,
                                     AdapterCommand command) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.sendCommAdapterCommand(ref, command)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void sendCommAdapterMessage(ClientID clientId,
                                     TCSObjectReference<Vehicle> vehicleRef,
                                     Object message) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.sendCommAdapterMessage(vehicleRef, message)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(ClientID clientId,
                                            TCSObjectReference<Vehicle> ref,
                                            Vehicle.IntegrationLevel integrationLevel)
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehicleIntegrationLevel(ref, integrationLevel)
      ).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleProcessableCategories(ClientID clientId,
                                                 TCSObjectReference<Vehicle> ref,
                                                 Set<String> processableCategories) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehicleProcessableCategories(ref, processableCategories))
          .get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }
}
