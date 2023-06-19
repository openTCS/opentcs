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
      orderKey = "0_enable")
  boolean enable();

  @ConfigurationEntry(
      type = "Integer",
      description = "The adapter's command queue capacity.",
      orderKey = "1_attributes_1")
  int commandQueueCapacity();

  @ConfigurationEntry(
      type = "String",
      description = "The string to be treated as a recharge operation.",
      orderKey = "1_attributes_2")
  String rechargeOperation();

  @ConfigurationEntry(
      type = "Double",
      description = {"The simulation time factor.",
                     "1.0 is real time, greater values speed up simulation."},
      orderKey = "2_behaviour_1")
  double simulationTimeFactor();

  @ConfigurationEntry(
      type = "Integer",
      description = {"The virtual vehicle's length in mm when it's loaded."},
      orderKey = "2_behaviour_2")
  int vehicleLengthLoaded();

  @ConfigurationEntry(
      type = "Integer",
      description = {"The virtual vehicle's length in mm when it's unloaded."},
      orderKey = "2_behaviour_3")
  int vehicleLengthUnloaded();
}
