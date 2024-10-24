// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import org.opentcs.kernel.workingset.WorkingSetCleanupTask;

/**
 * Provides methods to configure the {@link WorkingSetCleanupTask}.
 */
@ConfigurationPrefix(OrderPoolConfiguration.PREFIX)
public interface OrderPoolConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "orderpool";

  @ConfigurationEntry(
      type = "Long",
      description = "The interval between sweeps (in ms).",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL
  )
  long sweepInterval();

  @ConfigurationEntry(
      type = "Integer",
      description = "The minimum age of orders or peripheral jobs to remove in a sweep (in ms).",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY
  )
  int sweepAge();
}
