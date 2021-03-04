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
import org.slf4j.LoggerFactory;

/**
 * Provides instances of {@link SslRMIClientSocketFactory} and {@link SslRMIServerSocketFactory}.
 * Since these factories don't support anonymous cipher suites a keystore on the server-side and a
 * truststore on the client-side is necessary.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SslSocketFactoryProvider
    implements SocketFactoryProvider {

  /**
   * This class's logger.
   */
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SslSocketFactoryProvider.class);
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
   * The application's home directory to look for the keystore and truststore files.
   */
  private final String appHomeDir;
  /**
   * The password for the keystore.
   */
  private final String keystorePassword;
  /**
   * The password for the truststore.
   */
  private final String truststorePassword;

  /**
   * Creates a new instance.
   *
   * @deprecated Use one of the parameterized constructors instead.
   */
  @Deprecated
  public SslSocketFactoryProvider() {
    this(".", "password", "password");
  }

  /**
   * Creates a new instance.
   * Use this for clients (that only work with a truststore).
   *
   * @param appHomeDir The application's home directory to look for the keystore and truststore
   * files.
   * @param truststorePassword The password for the truststore.
   */
  public SslSocketFactoryProvider(String appHomeDir, String truststorePassword) {
    // Use an empty string (not null) as the keystore password to ensure the integrity of the 
    // keystore data gets checked, if a client is trying to load and access the keystore.
    this(appHomeDir, "", truststorePassword);
  }

  /**
   * Creates a new instance.
   *
   * @param appHomeDir The application's home directory to look for the keystore and truststore
   * files.
   * @param keystorePassword The password for the keystore.
   * @param truststorePassword The password for the truststore.
   */
  public SslSocketFactoryProvider(String appHomeDir,
                                  String keystorePassword,
                                  String truststorePassword) {
    this.appHomeDir = requireNonNull(appHomeDir, "appHomeDir");
    this.keystorePassword = requireNonNull(keystorePassword, "keystorePassword");
    this.truststorePassword = requireNonNull(truststorePassword, "truststorePassword");
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
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(new FileInputStream(appHomeDir + "/config/keystore.jks"),
              keystorePassword.toCharArray());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEY_TRUST_MANAGEMENT_ALGORITHM);
      kmf.init(ks, keystorePassword.toCharArray());

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
      KeyStore ts = KeyStore.getInstance("JKS");
      ts.load(new FileInputStream(appHomeDir + "/config/truststore.jks"),
              truststorePassword.toCharArray());
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
