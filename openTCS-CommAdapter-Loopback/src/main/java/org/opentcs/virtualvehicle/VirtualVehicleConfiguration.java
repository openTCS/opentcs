/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure to {@link LoopbackCommunicationAdapter}.
 */
@ConfigurationPrefix(VirtualVehicleConfiguration.PREFIX)
public interface VirtualVehicleConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "virtualvehicle";

  @ConfigurationEntry(
      type = "Boolean",
      description = "Whether to enable to register/enable the loopback driver.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_APPLICATION_START,
      orderKey = "0_enable")
  boolean enable();

  @ConfigurationEntry(
      type = "Integer",
      description = "The adapter's command queue capacity.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_attributes_1")
  int commandQueueCapacity();

  @ConfigurationEntry(
      type = "String",
      description = "The string to be treated as a recharge operation.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_attributes_2")
  String rechargeOperation();

  @ConfigurationEntry(
      type = "Double",
      description = "The rate at which the vehicle recharges in percent per second.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_attributes_3")
  double rechargePercentagePerSecond();

  @ConfigurationEntry(
      type = "Double",
      description = {"The simulation time factor.",
                     "1.0 is real time, greater values speed up simulation."},
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_behaviour_1")
  double simulationTimeFactor();

  @ConfigurationEntry(
      type = "Integer",
      description = {"The virtual vehicle's length in mm when it's loaded."},
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_behaviour_2")
  int vehicleLengthLoaded();

  @ConfigurationEntry(
      type = "Integer",
      description = {"The virtual vehicle's length in mm when it's unloaded."},
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "2_behaviour_3")
  int vehicleLengthUnloaded();
}
