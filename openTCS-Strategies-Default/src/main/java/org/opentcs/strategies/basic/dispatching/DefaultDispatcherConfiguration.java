/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultDispatcher}
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(DefaultDispatcherConfiguration.PREFIX)
public interface DefaultDispatcherConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultdispatcher";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create parking orders idle vehicles.",
      orderKey = "0_idle_0")
  boolean parkIdleVehicles();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically create recharge orders for idle vehicles.",
      orderKey = "0_idle_1")
  boolean rechargeIdleVehicles();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether orders to the current position with no operation should be assigned.",
      orderKey = "1_misc")
  boolean assignRedundantOrders();
  
  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether unroutable incoming transport orders should be marked as UNROUTABLE.",
      orderKey = "1_misc")
  boolean dismissUnroutableTransportOrders();
}
