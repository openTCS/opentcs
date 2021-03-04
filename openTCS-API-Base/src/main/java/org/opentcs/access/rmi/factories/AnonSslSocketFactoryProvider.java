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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instances of {@link SslRMIClientSocketFactory} and {@link SslRMIServerSocketFactory}.
 * Other than their default implementations the factories provided here not only support, but also
 * enable anonymous cipher suites (and only anonymous cipher suites). This is necessary for RMI
 * (with SSL) when there's no keystore on the server-side and no truststore on the client-side.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @deprecated Use {@link SecureSocketFactoryProvider} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class AnonSslSocketFactoryProvider
    implements SocketFactoryProvider {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AnonSslSocketFactoryProvider.class);

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    return new AnonSslClientSocketFactory();
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    return new AnonSslServerSocketFactory();
  }

  /**
   * Returns an array of anonym cipher suits supported by the default {@link SSLContext} or
   * {@code null}, if accessing the default SSLContext fails.
   * <p>
   * {@link SslRMIClientSocketFactory} and {@link SslRMIServerSocketFactory} and therefore
   * {@link AnonSslClientSocketFactory} and {@link AnonSslServerSocketFactory} use the
   * default SSLContext to create SSL sockets (unless it is set explicitly).
   * The default SSLContext is therefore used to access the supported chipher suites and filter
   * the anonym ones.
   * </p>
   * Note: Getting the default SSLContext only works, if the system properties for keystore and
   * truststore are not set or if they are set and the corresponding files exist.
   *
   * @return An array of anonym cipher suits supported by the default ssl context or {@code null},
   * if accessing the default SSLContext fails.
   */
  @Nullable
  public static String[] getAnonymousCipherSuites() {
    try {
      SSLParameters parameters = SSLContext.getDefault().getSupportedSSLParameters();
      List<String> anonymousCipherSuites = new ArrayList<>();
      for (String supportedCipherSuite : parameters.getCipherSuites()) {
        if (supportedCipherSuite.toLowerCase().contains("anon")) {
          anonymousCipherSuites.add(supportedCipherSuite);
        }
      }
      return anonymousCipherSuites.toArray(new String[anonymousCipherSuites.size()]);
    }
    catch (NoSuchAlgorithmException ex) {
      LOG.error("Error accessing the default SSLContext.", ex);
      return null;
    }
  }
}
