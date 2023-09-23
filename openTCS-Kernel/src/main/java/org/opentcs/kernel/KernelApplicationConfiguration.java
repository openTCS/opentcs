/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides common kernel configuration entries.
 */
@ConfigurationPrefix(KernelApplicationConfiguration.PREFIX)
public interface KernelApplicationConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "kernelapp";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically enable drivers on startup.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_startup_0")
  boolean autoEnableDriversOnStartup();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to automatically enable peripheral drivers on startup.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_startup_1")
  boolean autoEnablePeripheralDriversOnStartup();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving modelling state.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "2_autosave")
  boolean saveModelOnTerminateModelling();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly save the model when leaving operating state.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "2_autosave")
  boolean saveModelOnTerminateOperating();

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to implicitly update the router's topology when a path is (un)locked.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "3_topologyUpdate")
  boolean updateRoutingTopologyOnPathLockChange();
}
