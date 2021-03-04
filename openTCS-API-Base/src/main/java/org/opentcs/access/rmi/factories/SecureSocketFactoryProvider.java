/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
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
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import org.opentcs.access.SslParameterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instances of {@link SslRMIClientSocketFactory} and {@link SslRMIServerSocketFactory}.
 * Since these factories don't support anonymous cipher suites a keystore on the server-side and a
 * truststore on the client-side is necessary.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SecureSocketFactoryProvider
    implements SocketFactoryProvider {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SecureSocketFactoryProvider.class);
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
   * The SSL parameters to be used for creating socket factories.
   */
  private final SslParameterSet sslParameterSet;

  /**
   * Creates a new instance.
   *
   * @param sslParameterSet The parameter set to be used.
   */
  public SecureSocketFactoryProvider(SslParameterSet sslParameterSet) {
    this.sslParameterSet = requireNonNull(sslParameterSet, "sslParameterSet");
  }

  /**
   * Creates a new instance.
   *
   * @param keystoreFile The file url of the keystore.
   * @param truststoreFile The file url of the truststore.
   * @param keystoreType The expected type of the keystore.
   * @param keystorePassword The password for the keystore.
   * @param truststorePassword The password for the truststore.
   */
  public SecureSocketFactoryProvider(String keystoreType,
                                     File keystoreFile,
                                     String keystorePassword,
                                     File truststoreFile,
                                     String truststorePassword) {
    this.sslParameterSet = new SslParameterSet(keystoreType,
                                               keystoreFile,
                                               keystorePassword,
                                               truststoreFile,
                                               truststorePassword);
  }

  @Override
  public RMIClientSocketFactory getClientSocketFactory() {
    prepareClientSSLContext();
    return new SslRMIClientSocketFactory();
  }

  @Override
  public RMIServerSocketFactory getServerSocketFactory() {
    prepareServerSSLContext();
    return new SslRMIServerSocketFactory();
  }

  private void prepareServerSSLContext() {
    try {
      KeyStore ks = KeyStore.getInstance(sslParameterSet.getKeystoreType());
      ks.load(new FileInputStream(sslParameterSet.getKeystoreFile()),
              sslParameterSet.getKeystorePassword().toCharArray());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_TRUST_MANAGEMENT_ALGORITHM);
      kmf.init(ks, sslParameterSet.getKeystorePassword().toCharArray());

      SSLContext context = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
      context.init(kmf.getKeyManagers(), null, null);

      SSLContext.setDefault(context);
    }
    catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
               | KeyManagementException | UnrecoverableKeyException ex) {
      LOG.error("Error preparing the server's ssl context", ex);
    }
  }

  private void prepareClientSSLContext() {
    try {
      KeyStore ts = KeyStore.getInstance(sslParameterSet.getKeystoreType());
      ts.load(new FileInputStream(sslParameterSet.getTruststoreFile()),
              sslParameterSet.getTruststorePassword().toCharArray());
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(KEY_TRUST_MANAGEMENT_ALGORITHM);
      tmf.init(ts);

      SSLContext context = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
      context.init(null, tmf.getTrustManagers(), null);

      SSLContext.setDefault(context);
    }
    catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException
               | KeyManagementException ex) {
      LOG.error("Error preparing the client's ssl context", ex);
    }
  }
}
