/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.communication;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides helper methods for working with RMI registries.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class RMIRegistries {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(RMIRegistries.class.getName());

  /**
   * Prevents creation of instances.
   */
  private RMIRegistries() {
  }

  /**
   * Checks if a usable registry is available on the given host and port.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return <code>true</code> if, and only if, a usable registry was found on
   * the given host and port.
   */
  public static boolean registryAvailable(String host, int port) {
    return getWorkingRegistry(host, port) != null;
  }

  /**
   * Returns a reference to a working registry on the given host and port, or
   * <code>null</code>, if that's not possible.
   *
   * @param host The host to check.
   * @param port The port to check.
   * @return A reference to a working registry on the given host and port, or
   * <code>null</code>, if a working one was not found there.
   */
  public static Registry getWorkingRegistry(String host, int port) {
    Registry registry;
    log.fine("Checking for working RMI registry on " + host + ":" + port + ".");
    try {
      registry = LocateRegistry.getRegistry(host, port);
      String[] boundNames = registry.list();
    }
    catch (RemoteException exc) {
      log.fine("RMI registry on " + host + ":" + port + "unavailable.");
      return null;
    }
    return registry;
  }

  /**
   * Returns a reference to a working local registry, or <code>null</code>, if
   * that's impossible.
   * This method first checks if a local registry at the given port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, <code>null</code> is
   * returned.
   *
   * @param port The port at which the registry should be listening.
   * @return A reference to a working local registry, or <code>null</code>, if
   * getting or creating a working one was not possible.
   */
  public static Registry getOrCreateWorkingRegistry(int port) {
    Registry registry;
    String[] boundNames;
    log.fine("Checking for local RMI registry...");
    try {
      // Try to get a reference to an operating registry and test it.
      registry = LocateRegistry.getRegistry(port);
      boundNames = registry.list();
    }
    catch (RemoteException exc) {
      // No registry available, yet...
      log.fine("Local RMI registry unavailable, trying to create one...");
      try {
        // Try to create a new local registry and test it.
        registry = LocateRegistry.createRegistry(port);
        boundNames = registry.list();
      }
      catch (RemoteException anotherExc) {
        // Couldn't create a working registry, either - give up.
        log.log(Level.WARNING,
                "Could not get or create a usable registry, giving up.",
                anotherExc);
        registry = null;
      }
    }
    return registry;
  }
  
  
  /**
   * Returns a reference to a working local registry, or <code>null</code>, if
   * that's impossible.
   * This method first checks if a local registry at the default port is
   * available and usable. If so, a reference to it is returned, otherwise a new
   * one is created. If that is not possible, either, <code>null</code> is
   * returned.
   *
   * @return A reference to a working local registry, or <code>null</code>, if
   * getting or creating a working one was not possible.
   */
  public static Registry getOrCreateWorkingRegistry() {
    return getOrCreateWorkingRegistry(Registry.REGISTRY_PORT);
  }
}
