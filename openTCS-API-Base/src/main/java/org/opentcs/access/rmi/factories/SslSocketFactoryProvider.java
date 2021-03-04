/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import java.io.File;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Deprecated.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @deprecated Use {@link SecureSocketFactoryProvider} instead.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed in favor of the superclass")
public class SslSocketFactoryProvider
    extends SecureSocketFactoryProvider {

  /**
   * Creates a new instance.
   *
   * @deprecated Use {@link SecureSocketFactoryProvider} instead.
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
   * @deprecated Use {@link SecureSocketFactoryProvider} instead.
   */
  @Deprecated
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
   * @deprecated Use {@link SecureSocketFactoryProvider} instead.
   */
  @Deprecated
  public SslSocketFactoryProvider(String appHomeDir,
                                  String keystorePassword,
                                  String truststorePassword) {
    super("PKCS12",
          new File(appHomeDir, "/config/keystore.p12"),
          keystorePassword,
          new File(appHomeDir, "/config/truststore.p12"),
          truststorePassword);
  }
}
