/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.factories;

import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * Extends the {@link SslRMIServerSocketFactory} by enabling anonymous cipher suites 
 * (see {@link AnonSslSocketFactoryProvider#getAnonymousCipherSuites()}).
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class AnonSslServerSocketFactory
    extends SslRMIServerSocketFactory {

  public AnonSslServerSocketFactory() {
    super(AnonSslSocketFactoryProvider.getAnonymousCipherSuites(), null, false);
  }
}
