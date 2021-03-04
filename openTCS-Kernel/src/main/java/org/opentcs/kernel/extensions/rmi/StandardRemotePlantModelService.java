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
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemotePlantModelService;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.kernel.util.RegistryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemotePlantModelService} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * declared as {@link RemotePlantModelService#getRegistrationName()}.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardRemotePlantModelService
    extends StandardRemoteTCSObjectService
    implements RemotePlantModelService {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemotePlantModelService.class);
  /**
   * The plant model service to invoke methods on.
   */
  private final PlantModelService plantModelService;
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
   * @param plantModelService The plant model service.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider The provider for the registry with which this remote service registers.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public StandardRemotePlantModelService(PlantModelService plantModelService,
                                         UserManager userManager,
                                         RmiKernelInterfaceConfiguration configuration,
                                         SocketFactoryProvider socketFactoryProvider,
                                         RegistryProvider registryProvider,
                                         @KernelExecutor ExecutorService kernelExecutor) {
    super(plantModelService, userManager, kernelExecutor);
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
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
                                       configuration.remotePlantModelServicePort(),
                                       socketFactoryProvider.getClientSocketFactory(),
                                       socketFactoryProvider.getServerSocketFactory());
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(RegistrationName.REMOTE_PLANT_MODEL_SERVICE, this);
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
      rmiRegistry.unbind(RegistrationName.REMOTE_PLANT_MODEL_SERVICE);
      LOG.debug("Unexporting RMI interface...");
      UnicastRemoteObject.unexportObject(this, true);
    }
    catch (RemoteException | NotBoundException exc) {
      LOG.warn("Exception shutting down RMI interface", exc);
    }

    initialized = false;
  }

  @Override
  public void createPlantModel(ClientID clientId, PlantModelCreationTO to) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_MODEL);

    try {
      kernelExecutor.submit(() -> plantModelService.createPlantModel(to)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  @Deprecated
  public String getLoadedModelName(ClientID clientId) {
    return getModelName(clientId);
  }

  @Override
  public String getModelName(ClientID clientId) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return plantModelService.getModelName();
  }

  @Override
  public Map<String, String> getModelProperties(ClientID clientId) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return plantModelService.getModelProperties();
  }

  @Override
  @Deprecated
  public String getPersistentModelName(ClientID clientId) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return plantModelService.getPersistentModelName();
  }
}
