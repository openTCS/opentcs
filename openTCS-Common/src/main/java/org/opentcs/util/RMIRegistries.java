/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.access.rmi.factories.SocketFactoryProvider;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides helper methods for working with RMI registries.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link org.opentcs.kernel.util.RegistryProvider} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed")
public final class RMIRegistries {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RMIRegistries.class);
  /**
   * Provides socket factories used to create RMI registries.
   */
  private final SocketFactoryProvider socketFactoryProvider;

  /**
   * Prevents creation of instances.
   *
   * @param socketFactoryProvider Provides socket factories used to create RMI registries.
   */
  public RMIRegistries(@Nonnull SocketFactoryProvider socketFactoryProvider) {
    this.socketFactoryProvider = requireNonNull(socketFactoryProvider, "socketFactoryProvider");
  }

  /**
   * Checks if a usable registry is available on the given host and port.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return <code>true</code> if, and only if, a usable registry was found on
   * the given host and port.
   */
  public boolean registryAvailable(String host, int port) {
    return lookupRegistry(host, port).isPresent();
  }

  /**
   * Returns a reference to a working registry on the given host and port, if there is one.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return A reference to a working registry on the given host and port, if a
   * working one was found there.
   */
  public Optional<Registry> lookupRegistry(String host, int port) {
    Registry registry;
    LOG.debug("Checking for working RMI registry on {}:{}", host, port);
    try {
      registry = LocateRegistry.getRegistry(host,
                                            port,
                                            socketFactoryProvider.getClientSocketFactory());
      String[] boundNames = registry.list();
    }
    catch (RemoteException exc) {
      LOG.debug("RMI registry on {}:{} unavailable", host, port);
      return Optional.empty();
    }
    return Optional.of(registry);
  }

  /**
   * Returns a reference to a working local registry, if one already existed or a new one could be
   * installed.
   * This method first checks if a local registry at the given port is available and usable. If so,
   * a reference to it is returned, otherwise a new one is created. If that is not possible, either,
   * no reference is returned.
   *
   * @param port The port at which the registry should be listening.
   * @return A reference to a working local registry, if getting or creating a working one was
   * possible.
   */
  public Optional<Registry> lookupOrInstallRegistry(int port) {
    Registry registry;
    String[] boundNames;
    LOG.debug("Checking for local RMI registry...");
    try {
      // Try to get a reference to an operating registry and test it.
      registry = LocateRegistry.getRegistry(port);
      boundNames = registry.list();
    }
    catch (RemoteException exc) {
      // No registry available, yet...
      LOG.debug("Local RMI registry unavailable, trying to create one...");
      try {
        // Try to create a new local registry and test it.
        registry = LocateRegistry.createRegistry(port,
                                                 socketFactoryProvider.getClientSocketFactory(),
                                                 socketFactoryProvider.getServerSocketFactory());
        boundNames = registry.list();
      }
      catch (RemoteException anotherExc) {
        // Couldn't create a working registry, either - give up.
        LOG.warn("Could not get or create a usable registry, giving up.",
                 anotherExc);
        registry = null;
      }
    }
    return Optional.ofNullable(registry);
  }

  /**
   * Returns a reference to a working local registry, or <code>null</code>, if that's impossible.
   * This method first checks if a local registry at the default port is available and usable.
   * If so, a reference to it is returned, otherwise a new one is created. If that is not possible,
   * either, <code>null</code> is returned.
   *
   * @return A reference to a working local registry, or <code>null</code>, if getting or creating
   * a working one was not possible.
   */
  public Optional<Registry> lookupOrInstallRegistry() {
    return lookupOrInstallRegistry(Registry.REGISTRY_PORT);
  }
}
