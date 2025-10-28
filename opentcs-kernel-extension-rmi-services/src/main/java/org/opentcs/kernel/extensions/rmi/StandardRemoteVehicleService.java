// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.rmi;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemoteVehicleService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.AcceptableOrderType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.EnergyLevelThresholdSet;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.drivers.vehicle.management.VehicleAttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemoteVehicleService} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * {@link RegistrationName#REMOTE_VEHICLE_SERVICE}.
 * </p>
 */
public class StandardRemoteVehicleService
    extends
      StandardRemoteTCSObjectService
    implements
      RemoteVehicleService {

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
  public StandardRemoteVehicleService(
      VehicleService vehicleService,
      UserManager userManager,
      RmiKernelInterfaceConfiguration configuration,
      SocketFactoryProvider socketFactoryProvider,
      RegistryProvider registryProvider,
      @KernelExecutor
      ExecutorService kernelExecutor
  ) {
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
      UnicastRemoteObject.exportObject(
          this,
          configuration.remoteVehicleServicePort(),
          socketFactoryProvider.getClientSocketFactory(),
          socketFactoryProvider.getServerSocketFactory()
      );
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
  public void attachCommAdapter(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      VehicleCommAdapterDescription description
  ) {
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
  public VehicleAttachmentInformation fetchAttachmentInformation(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref
  ) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return vehicleService.fetchAttachmentInformation(ref);
  }

  @Override
  public VehicleProcessModelTO fetchProcessModel(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref
  ) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return vehicleService.fetchProcessModel(ref);
  }

  @Override
  public void sendCommAdapterMessage(
      ClientID clientId,
      TCSObjectReference<Vehicle> vehicleRef,
      VehicleCommAdapterMessage message
  ) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(() -> vehicleService.sendCommAdapterMessage(vehicleRef, message)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleIntegrationLevel(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      Vehicle.IntegrationLevel integrationLevel
  )
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
  public void updateVehiclePaused(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      boolean paused
  )
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehiclePaused(ref, paused)
      ).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleEnergyLevelThresholdSet(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      EnergyLevelThresholdSet energyLevelThresholdSet
  ) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehicleEnergyLevelThresholdSet(ref, energyLevelThresholdSet)
      )
          .get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleAcceptableOrderTypes(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      Set<AcceptableOrderType> acceptableOrderTypes
  ) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehicleAcceptableOrderTypes(ref, acceptableOrderTypes)
      )
          .get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void updateVehicleEnvelopeKey(
      ClientID clientId,
      TCSObjectReference<Vehicle> ref,
      String envelopeKey
  )
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_VEHICLES);

    try {
      kernelExecutor.submit(
          () -> vehicleService.updateVehicleEnvelopeKey(ref, envelopeKey)
      ).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }
}
