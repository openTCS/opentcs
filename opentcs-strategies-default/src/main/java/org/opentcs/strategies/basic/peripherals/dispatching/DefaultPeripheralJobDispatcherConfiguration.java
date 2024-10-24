// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.peripherals.dispatching;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultPeripheralJobDispatcher}
 */
@ConfigurationPrefix(DefaultPeripheralJobDispatcherConfiguration.PREFIX)
public interface DefaultPeripheralJobDispatcherConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "defaultperipheraljobdispatcher";

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval between redispatching of peripheral devices.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "9_misc"
  )
  long idlePeripheralRedispatchingInterval();
}
