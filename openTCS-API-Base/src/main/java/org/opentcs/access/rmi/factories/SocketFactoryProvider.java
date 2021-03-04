/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import javax.annotation.Nullable;

/**
 * A provider for instances of {@link RMIClientSocketFactory} and {@link RMIServerSocketFactory}.
 * Generally one provider should provide compatible factories for clients and servers.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface SocketFactoryProvider {

  /**
   * Returns a {@link RMIClientSocketFactory}.
   *
   * @return A {@link RMIClientSocketFactory}.
   * May be <code>null</code> to indicate that a default factory implementation is to be used.
   */
  @Nullable
  RMIClientSocketFactory getClientSocketFactory();

  /**
   * Returns a {@link RMIServerSocketFactory}.
   *
   * @return A {@link RMIServerSocketFactory}.
   * May be <code>null</code> to indicate that a default factory implementation is to be used.
   */
  @Nullable
  RMIServerSocketFactory getServerSocketFactory();
}
