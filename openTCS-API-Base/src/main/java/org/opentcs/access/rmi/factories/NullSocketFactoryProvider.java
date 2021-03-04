/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * Provides {@code null} for both client and server socket factories.
 * By using this provider, the default client-side/server-side socket factory will be used in
 * {@link Registry} stubs.
 * 
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class NullSocketFactoryProvider
    implements SocketFactoryProvider {

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    return null;
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    return null;
  }
}
