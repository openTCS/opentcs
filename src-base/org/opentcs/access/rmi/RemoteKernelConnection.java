/*
 * openTCS copyright information:
 * Copyright (c) 2010 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import org.opentcs.access.CredentialsException;

/**
 * Provides methods for logging into and out of a remote kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface RemoteKernelConnection {

  /**
   * Logs in with/establishes a connection to the remote kernel.
   *
   * @throws CredentialsException If the credentials used (user name and
   * passoword) were not accepted by the remote kernel.
   */
  void login()
      throws CredentialsException;

  /**
   * Logs out from/drops the connection to the remote kernel.
   */
  void logout();

  /**
   * Returns this connection's state.
   *
   * @return This connection's state.
   */
  State getConnectionState();

  /**
   * Defines the states in which a KernelProxy instance may be in.
   */
  public enum State {

    /**
     * Indicates the proxy is not connected to a remote kernel.
     * While in this state, calls to kernel methods will result in a
     * KernelUnavailableException.
     */
    DISCONNECTED,
    /**
     * Indicates the proxy is connected and logged in to a remote kernel, thus
     * in a usable state.
     */
    LOGGED_IN
  }
}
