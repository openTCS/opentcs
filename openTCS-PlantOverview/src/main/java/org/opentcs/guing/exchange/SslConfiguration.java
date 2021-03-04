/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the ssl connection.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
@ConfigurationPrefix(SslConfiguration.PREFIX)
public interface SslConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "ssl";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to use SSL to encrypt RMI connections to the kernel.",
      orderKey = "0_connection_0")
  boolean enable();

  @ConfigurationEntry(
      type = "String",
      description = "The path to the SSL truststore.",
      orderKey = "0_connection_1")
  String truststoreFile();

  @ConfigurationEntry(
      type = "String",
      description = "The password for the SSL truststore.",
      orderKey = "0_connection_2")
  String truststorePassword();
}
