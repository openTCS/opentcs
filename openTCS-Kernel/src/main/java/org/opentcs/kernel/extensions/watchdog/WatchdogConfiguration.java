/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Configuration for the watchdog extension.
 */
@ConfigurationPrefix(WatchdogConfiguration.PREFIX)
public interface WatchdogConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "watchdog";

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval (in milliseconds) in which to check for block consistency.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "1_block"
  )
  int blockConsistencyCheckInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "The interval (in milliseconds) in which to check for stranded vehicles.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "2_stranded_vehicle_0"
  )
  int strandedVehicleCheckInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "The duration (in milliseconds) that a vehicle must be in a _stranded_ state "
          + "to actually be considered stranded.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_stranded_vehicle_1"
  )
  int strandedVehicleDurationThreshold();
}
