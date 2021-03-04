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
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.rmi.ClientID;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.access.rmi.services.RegistrationName;
import org.opentcs.access.rmi.services.RemoteKernelServicePortal;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.kernel.extensions.rmi.UserManager.ClientEntry;
import org.opentcs.kernel.util.RegistryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the standard implementation of the {@link RemoteKernelServicePortal} interface.
 * <p>
 * Upon creation, an instance of this class registers itself with the RMI registry by the name
 * declared as {@link RemoteKernelServicePortal#getRegistrationName()}.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardRemoteKernelClientPortal
    implements RemoteKernelServicePortal,
               KernelExtension {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StandardRemoteKernelClientPortal.class);
  /**
   * The kernel.
   */
  private final Kernel kernel;
  /**
   * The kernel's remote services.
   */
  private final Set<KernelRemoteService> remoteServices;
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
   * Provides the registry with which this remote portal registers.
   */
  private final RegistryProvider registryProvider;
  /**
   * The registry with which this remote portal registers.
   */
  private Registry rmiRegistry;
  /**
   * Whether this remote portal is initialized or not.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param remoteServices The kernel's remote services.
   * @param userManager The user manager.
   * @param configuration This class' configuration.
   * @param socketFactoryProvider The socket factory provider used for RMI.
   * @param registryProvider The provider for the registry with which this remote portal registers.
   */
  @Inject
  public StandardRemoteKernelClientPortal(LocalKernel kernel,
                                          Set<KernelRemoteService> remoteServices,
                                          UserManager userManager,
                                          RmiKernelInterfaceConfiguration configuration,
                                          SocketFactoryProvider socketFactoryProvider,
                                          RegistryProvider registryProvider) {
    this.kernel = requireNonNull(kernel, "kernel");
    this.remoteServices = requireNonNull(remoteServices, "remoteServices");
    this.userManager = requireNonNull(userManager, "userManager");
    this.configuration = requireNonNull(configuration, "configuration");
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
    this.registryProvider = requireNonNull(registryProvider, "registryProvider");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    registryProvider.initialize();
    userManager.initialize();

    rmiRegistry = registryProvider.get();
    // Export this instance via RMI.
    try {
      LOG.debug("Exporting proxy...");
      UnicastRemoteObject.exportObject(this,
                                       configuration.remoteKernelServicePortalPort(),
                                       socketFactoryProvider.getClientSocketFactory(),
                                       socketFactoryProvider.getServerSocketFactory());
      LOG.debug("Binding instance with RMI registry...");
      rmiRegistry.rebind(RegistrationName.REMOTE_KERNEL_CLIENT_PORTAL, this);
      LOG.debug("Bound instance {} with registry {}.", rmiRegistry.list(), rmiRegistry);
    }
    catch (RemoteException exc) {
      LOG.error("Could not export or bind with RMI registry", exc);
      return;
    }

    for (KernelRemoteService remoteService : remoteServices) {
      remoteService.initialize();
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

    for (KernelRemoteService remoteService : remoteServices) {
      remoteService.terminate();
    }

    try {
      LOG.debug("Unbinding from RMI registry...");
      rmiRegistry.unbind(RegistrationName.REMOTE_KERNEL_CLIENT_PORTAL);
      LOG.debug("Unexporting RMI interface...");
      UnicastRemoteObject.unexportObject(this, true);
    }
    catch (RemoteException | NotBoundException exc) {
      LOG.warn("Exception shutting down RMI interface", exc);
    }

    userManager.terminate();
    registryProvider.terminate();
    initialized = false;
  }

  @Override
  @SuppressWarnings("deprecation")
  public ClientID login(String userName, String password, Predicate<Object> eventFilter)
      throws CredentialsException {
    requireNonNull(userName, "userName");
    requireNonNull(password, "password");

    synchronized (userManager.getKnownClients()) {
      UserAccount account = userManager.getUser(userName);
      if (account == null || !account.getPassword().equals(password)) {
        LOG.debug("Authentication failed for user {}.", userName);
        throw new CredentialsException("Authentication failed for user " + userName);
      }

      // Generate a new ID for the client.
      ClientID clientId = new ClientID(userName);
      // Add an entry for the newly connected client.
      ClientEntry clientEntry = new ClientEntry(userName, account.getPermissions());
      clientEntry.getEventBuffer().setEventFilter(eventFilter);
      userManager.getKnownClients().put(clientId, clientEntry);
      LOG.debug("New client named {} logged in", clientId.getClientName());
      return clientId;
    }
  }

  @Override
  public void logout(ClientID clientID) {
    requireNonNull("clientID");

    // Forget the client so it won't be able to call methods on this kernel and won't receive 
    // events any more.
    synchronized (userManager.getKnownClients()) {
      userManager.getKnownClients().remove(clientID);
      LOG.debug("Client named {} logged out", clientID.getClientName());
    }
  }

  @Override
  public Kernel.State getState(ClientID clientId) {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return kernel.getState();
  }

  @Override
  public List<Object> fetchEvents(ClientID clientId, long timeout)
      throws RemoteException {
    userManager.verifyCredentials(clientId, UserPermission.READ_DATA);

    return userManager.pollEvents(clientId, timeout);
  }
}
