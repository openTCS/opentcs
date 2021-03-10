/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import static java.util.Objects.requireNonNull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * This implementation is similar to {@link SslRMIClientSocketFactory} but allows the use of a
 * custom SSLConext.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
class CustomSslRMIClientSocketFactory
    implements RMIClientSocketFactory,
               Serializable {

  /**
   * Provides an instance of {@link SSLContext} used to get the actual socket factory.
   */
  private final SecureSslContextFactory secureSslContextFactory;

  /**
   * Creates a new instance.
   *
   * @param secureSslContextFactory Provides an instance of {@link SSLContext} used to get the
   * actual socket factory.
   */
  public CustomSslRMIClientSocketFactory(SecureSslContextFactory secureSslContextFactory) {
    this.secureSslContextFactory = requireNonNull(secureSslContextFactory,
                                                  "secureSslContextFactory");
  }

  @Override
  public Socket createSocket(String host, int port)
      throws IOException {
    SSLContext context = secureSslContextFactory.createClientContext();
    SSLSocketFactory sf = context.getSocketFactory();
    SSLSocket socket = (SSLSocket) sf.createSocket(host, port);
    SSLParameters param = context.getSupportedSSLParameters();
    socket.setEnabledCipherSuites(param.getCipherSuites());
    socket.setEnabledProtocols(param.getProtocols());
    return socket;
  }
}
