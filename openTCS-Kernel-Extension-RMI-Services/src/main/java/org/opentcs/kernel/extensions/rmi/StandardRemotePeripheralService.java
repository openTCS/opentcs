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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemotePeripheralService;
import org.opentcs.components.kernel.services.PeripheralService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralAdapterCommand;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;
import org.opentcs.drivers.peripherals.management.PeripheralAttachmentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemotePeripheralService} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * {@link RegistrationName#REMOTE_PERIPHERAL_SERVICE}.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardRemotePeripheralService
    extends StandardRemoteTCSObjectService
    implements RemotePeripheralService {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemotePeripheralService.class);
  /**
   * The peripheral service to invoke methods on.
   */
  private final PeripheralService peripheralService;
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
   * @param peripheralService The peripheral service.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider The provider for the registry with which this remote service registers.
   * @param kernelExecutor Executes tasks modifying kernel data.
   */
  @Inject
  public StandardRemotePeripheralService(PeripheralService peripheralService,
                                         UserManager userManager,
                                         RmiKernelInterfaceConfiguration configuration,
                                         SocketFactoryProvider socketFactoryProvider,
                                         RegistryProvider registryProvider,
                                         @KernelExecutor ExecutorService kernelExecutor) {
    super(peripheralService, userManager, kernelExecutor);
    this.peripheralService = requireNonNull(peripheralService, "peripheralService");
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
                                       configuration.remotePeripheralServicePort(),
                                       socketFactoryProvider.getClientSocketFactory(),
                                       socketFactoryProvider.getServerSocketFactory());
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(RegistrationName.REMOTE_PERIPHERAL_SERVICE, this);
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
      rmiRegistry.unbind(RegistrationName.REMOTE_PERIPHERAL_SERVICE);
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
                                TCSResourceReference<Location> ref,
                                PeripheralCommAdapterDescription description) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_PERIPHERALS);

    try {
      kernelExecutor.submit(() -> peripheralService.attachCommAdapter(ref, description)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void disableCommAdapter(ClientID clientId, TCSResourceReference<Location> ref) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_PERIPHERALS);

    try {
      kernelExecutor.submit(() -> peripheralService.disableCommAdapter(ref)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public void enableCommAdapter(ClientID clientId, TCSResourceReference<Location> ref) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_PERIPHERALS);

    try {
      kernelExecutor.submit(() -> peripheralService.enableCommAdapter(ref)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }

  @Override
  public PeripheralAttachmentInformation fetchAttachmentInformation(
      ClientID clientId,
      TCSResourceReference<Location> ref) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return peripheralService.fetchAttachmentInformation(ref);
  }

  @Override
  public PeripheralProcessModel fetchProcessModel(ClientID clientId,
                                                  TCSResourceReference<Location> ref) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return peripheralService.fetchProcessModel(ref);
  }

  @Override
  public void sendCommAdapterCommand(ClientID clientId,
                                     TCSResourceReference<Location> ref,
                                     PeripheralAdapterCommand command) {
    userManager.verifyCredentials(clientId, UserPermission.MODIFY_PERIPHERALS);

    try {
      kernelExecutor.submit(() -> peripheralService.sendCommAdapterCommand(ref, command)).get();
    }
    catch (InterruptedException | ExecutionException exc) {
      throw findSuitableExceptionFor(exc);
    }
  }
}
