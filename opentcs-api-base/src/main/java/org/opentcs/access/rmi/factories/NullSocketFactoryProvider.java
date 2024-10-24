// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.factories;

import jakarta.inject.Inject;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * Provides {@code null} for both client and server socket factories.
 * By using this provider, the default client-side/server-side socket factory will be used in
 * {@link Registry} stubs.
 */
public class NullSocketFactoryProvider
    implements
      SocketFactoryProvider {

  @Inject
  public NullSocketFactoryProvider() {
  }

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    return null;
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    return null;
  }
}
