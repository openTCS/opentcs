/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.peripheral.loopback;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure to {@link LoopbackPeripheralCommAdapter}.
 */
@ConfigurationPrefix(VirtualPeripheralConfiguration.PREFIX)
public interface VirtualPeripheralConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "virtualperipheral";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable to register/enable the peripheral loopback driver.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_enable")
  boolean enable();
}
