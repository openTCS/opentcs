/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Extends the {@link SslRMIClientSocketFactory} by enabling anonymous cipher suites
 * (see {@link AnonSslSocketFactoryProvider#getAnonymousCipherSuites()}).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @deprecated Explicit support for anonymous cipher suites will be removed.
 * Use {@link SecureSocketFactoryProvider}.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class AnonSslClientSocketFactory
    extends SslRMIClientSocketFactory {

  @Override
  public Socket createSocket(String string, int i)
      throws IOException {
    SSLSocket socket = (SSLSocket) super.createSocket(string, i);
    socket.setEnabledCipherSuites(AnonSslSocketFactoryProvider.getAnonymousCipherSuites());
    return socket;
  }
}
