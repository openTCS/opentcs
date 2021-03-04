/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import org.opentcs.access.Kernel;

/**
 * Declares methods for managing a connection/proxy to a remote kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface KernelProxyManager {

  /**
   * Tries to establish a connection to the kernel.
   *
   * @param host The name of the host running the kernel.
   * @param port The port to connect to.
   * @return <code>true</code> if, and only if, the connection was established
   * successfully.
   */
  boolean connect(String host, int port);

  /**
   * Tries to establish a connection to the kernel.
   *
   * @param connParamSet The parameters to be used for establishing the
   * connection.
   * @return <code>true</code> if, and only if, the connection was established
   * successfully.
   */
  boolean connect(ConnectionParamSet connParamSet);

  /**
   * Tells the proxy manager the connection to the kernel was lost.
   */
  void disconnect();

  /**
   * Checks whether a connection to the kernel is established.
   *
   * @return <code>true</code> if, and only if, a connection to the kernel is
   * established.
   */
  boolean isConnected();

  /**
   * Returns a reference to the remote kernel currently connected to.
   *
   * @return A reference to the remote kernel, or <code>null</code>, if not
   * connected.
   */
  Kernel kernel();

  /**
   * Returns the host currently connected to.
   *
   * @return The host currently connected to, or <code>null</code>, if not
   * connected.
   */
  String getHost();

  /**
   * Returns the port currently connected to.
   *
   * @return The port currently connected to, or <code>-1</code>, if not
   * connected.
   */
  int getPort();
}
