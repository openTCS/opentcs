// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.rmi.factories;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;

/**
 * This implementation is similar to {@link SslRMIClientSocketFactory} but allows the use of a
 * custom SSLConext.
 */
class CustomSslRMIClientSocketFactory
    implements
      RMIClientSocketFactory,
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
  CustomSslRMIClientSocketFactory(SecureSslContextFactory secureSslContextFactory) {
    this.secureSslContextFactory = requireNonNull(
        secureSslContextFactory,
        "secureSslContextFactory"
    );
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
