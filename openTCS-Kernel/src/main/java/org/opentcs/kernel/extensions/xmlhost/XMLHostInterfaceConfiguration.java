/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the XML-based host interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(XMLHostInterfaceConfiguration.PREFIX)
public interface XMLHostInterfaceConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "xmlhostinterface";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable the XML host interface.",
      orderKey = "0_general")
  boolean enable();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port on which to listen for incoming order connections.",
      orderKey = "1_orders_0")
  int ordersServerPort();

  @ConfigurationEntry(
      type = "Integer",
      description = "The time (in ms) after which idle connections are closed.",
      orderKey = "1_orders_1")
  int ordersIdleTimeout();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum number of bytes read from sockets before closing the connection.",
      orderKey = "1_orders_2")
  int ordersInputLimit();

  @ConfigurationEntry(
      type = "Integer",
      description = "The TCP port on which to listen for incoming status channel connections.",
      orderKey = "2_status_0")
  int statusServerPort();

  @ConfigurationEntry(
      type = "String",
      description = "A string to be used for separating subsequent status messages in the stream.",
      orderKey = "2_status_1")
  String statusMessageSeparator();
}
