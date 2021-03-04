/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi;

import javax.annotation.Nonnull;
import org.opentcs.access.CredentialsException;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides methods for logging into and out of a remote kernel.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use {@link KernelServicePortalBuilder} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public interface RemoteKernelConnection {

  /**
   * Logs in with/establishes a connection to the remote kernel.
   *
   * @throws CredentialsException If the credentials used (user name and password) were not accepted
   * by the remote kernel.
   * @throws KernelUnavailableException If there was a problem logging in with the remote kernel.
   */
  void login()
      throws CredentialsException, KernelUnavailableException;

  /**
   * Logs out from/drops the connection to the remote kernel.
   */
  void logout();

  /**
   * Returns this connection's state.
   *
   * @return This connection's state.
   */
  @Nonnull
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
