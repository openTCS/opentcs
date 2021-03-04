/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.ServiceUnavailableException;

/**
 * Declares methods for managing a connection to a remote portal.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface PortalManager {

  /**
   * Tries to establish a connection to the portal.
   *
   * @param mode The mode to use for the connection attempt.
   * @return {@code true} if, and only if, the connection was established successfully.
   */
  boolean connect(ConnectionMode mode);

  /**
   * Tells the portal manager the connection to the portal was lost.
   */
  void disconnect();

  /**
   * Checks whether a connection to the portal is established.
   *
   * @return {@code true} if, and only if, a connection to the portal is established.
   */
  boolean isConnected();

  /**
   * Returns the remote kernel client portal the manager is working with.
   *
   * @return The remote kernel client portal.
   */
  KernelServicePortal getPortal();

  /**
   * Returns a description for the current connection.
   *
   * @return A description for the current connection.
   */
  String getDescription();

  /**
   * Returns the host currently connected to.
   *
   * @return The host currently connected to, or {@code null}, if not connected.
   */
  String getHost();

  /**
   * Returns the port currently connected to.
   *
   * @return The port currently connected to, or {@code -1}, if not connected.
   */
  int getPort();

  /**
   * Defines the states in which a portal manager instance may be in.
   */
  public enum ConnectionState {

    /**
     * Indicates the portal manager is trying to connect to the remote portal.
     */
    CONNECTING,
    /**
     * Indicates the portal is connected and logged in to a remote portal, thus in a usable state.
     */
    CONNECTED,
    /**
     * Indicates the portal is disconnecting from the remote portal.
     */
    DISCONNECTING,
    /**
     * Indicates the portal is not connected to a remote portal.
     * While in this state, calls to the portal's service methods will result in a
     * {@link ServiceUnavailableException}.
     */
    DISCONNECTED;
  }

  /**
   * Defines the modes a portal manager uses to establish a connection to a portal.
   */
  public enum ConnectionMode {

    /**
     * Connect automatically by using a predefined set of connection parameters.
     */
    AUTO,
    /**
     * Connect manually by showing a dialog allowing to enter connection parameters.
     */
    MANUAL,
    /**
     * Connect to the portal we were previously connected to.
     */
    RECONNECT;
  }
}
