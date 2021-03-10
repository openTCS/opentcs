/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.File;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import org.opentcs.access.SslParameterSet;

/**
 * Provides instances of {@link RMIClientSocketFactory} and {@link RMIServerSocketFactory} that are
 * implemented over the SSL or TLS protocols.
 * Since these factories don't support anonymous cipher suites a keystore on the server-side and a
 * truststore on the client-side is necessary.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SecureSocketFactoryProvider
    implements SocketFactoryProvider {

  /**
   * Provides methods for creating client-side and server-side {@link SSLContext} instances.
   */
  private final SecureSslContextFactory secureSslContextFactory;

  /**
   * Creates a new instance.
   *
   * @param sslParameterSet The SSL parameters to be used for creating socket factories.
   */
  @Inject
  public SecureSocketFactoryProvider(SslParameterSet sslParameterSet) {
    requireNonNull(sslParameterSet, "sslParameterSet");
    this.secureSslContextFactory = new SecureSslContextFactory(sslParameterSet);
  }

  /**
   * Creates a new instance.
   *
   * @param keystoreFile The file url of the keystore.
   * @param truststoreFile The file url of the truststore.
   * @param keystoreType The expected type of the keystore.
   * @param keystorePassword The password for the keystore.
   * @param truststorePassword The password for the truststore.
   * @deprecated Use {@link #SecureSocketFactoryProvider(org.opentcs.access.SslParameterSet)}
   * instead.
   */
  @Deprecated
  public SecureSocketFactoryProvider(String keystoreType,
                                     File keystoreFile,
                                     String keystorePassword,
                                     File truststoreFile,
                                     String truststorePassword) {
    this(new SslParameterSet(keystoreType,
                             keystoreFile,
                             keystorePassword,
                             truststoreFile,
                             truststorePassword));
  }

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    return new CustomSslRMIClientSocketFactory(secureSslContextFactory);
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    SSLContext context = secureSslContextFactory.createServerContext();
    SSLParameters param = context.getSupportedSSLParameters();
    return new SslRMIServerSocketFactory(context,
                                         param.getCipherSuites(),
                                         param.getProtocols(),
                                         param.getWantClientAuth());
  }
}
