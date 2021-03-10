/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import static java.util.Objects.requireNonNull;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.opentcs.access.SslParameterSet;

/**
 * Provides methods for creating client-side and server-side {@link SSLContext} instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class SecureSslContextFactory
    implements Serializable {

  /**
   * The name of the algorithm to use for the {@link KeyManagerFactory} and
   * {@link TrustManagerFactory}.
   */
  private static final String KEY_TRUST_MANAGEMENT_ALGORITHM = "SunX509";
  /**
   * The protocol to use with the ssl context.
   */
  private static final String SSL_CONTEXT_PROTOCOL = "TLSv1.2";
  /**
   * The ssl parameters to be used for creating the ssl context.
   */
  private final SslParameterSet sslParameterSet;

  /**
   * Creates a new instance.
   *
   * @param sslParameterSet The ssl parameters to be used for creating the ssl context.
   */
  public SecureSslContextFactory(SslParameterSet sslParameterSet) {
    this.sslParameterSet = requireNonNull(sslParameterSet, "sslParameterSet");
  }

  /**
   * Creates an instance of {@link SSLContext} for the client.
   *
   * @return The ssl context.
   * @throws IllegalStateException If the creation of the ssl context fails.
   */
  public SSLContext createClientContext()
      throws IllegalStateException {
    SSLContext context = null;

    try {
      KeyStore ts = KeyStore.getInstance(sslParameterSet.getKeystoreType());
      ts.load(new FileInputStream(sslParameterSet.getTruststoreFile()),
              sslParameterSet.getTruststorePassword().toCharArray());
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(KEY_TRUST_MANAGEMENT_ALGORITHM);
      tmf.init(ts);

      context = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
      context.init(null, tmf.getTrustManagers(), null);
    }
    catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
               | KeyManagementException ex) {
      throw new IllegalStateException("Error creating the client's ssl context", ex);
    }

    return context;
  }

  /**
   * Creates an instance of {@link SSLContext} for the server.
   *
   * @return The ssl context.
   * @throws IllegalStateException If the creation of the ssl context fails.
   */
  public SSLContext createServerContext()
      throws IllegalStateException {
    SSLContext context = null;

    try {
      KeyStore ks = KeyStore.getInstance(sslParameterSet.getKeystoreType());
      ks.load(new FileInputStream(sslParameterSet.getKeystoreFile()),
              sslParameterSet.getKeystorePassword().toCharArray());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_TRUST_MANAGEMENT_ALGORITHM);
      kmf.init(ks, sslParameterSet.getKeystorePassword().toCharArray());

      context = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
      context.init(kmf.getKeyManagers(), null, null);

    }
    catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
               | KeyManagementException | UnrecoverableKeyException ex) {
      throw new IllegalStateException("Error creating the server's ssl context", ex);
    }

    return context;
  }
}
