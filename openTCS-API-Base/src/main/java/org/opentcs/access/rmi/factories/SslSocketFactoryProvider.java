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
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * Provides instances of {@link SslRMIClientSocketFactory} and {@link SslRMIServerSocketFactory}.
 * Since these factories don't support anonymous cipher suites a keystore on the server-side and a
 * truststore on the client-side is necessary.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SslSocketFactoryProvider
    implements SocketFactoryProvider {

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    System.setProperty("javax.net.ssl.trustStore", "./config/truststore.jks");
    System.setProperty("javax.net.ssl.trustStorePassword", "password");
    return new SslRMIClientSocketFactory();
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    System.setProperty("javax.net.ssl.keyStore", "./config/keystore.jks");
    System.setProperty("javax.net.ssl.keyStorePassword", "password");
    return new SslRMIServerSocketFactory();
  }
}
