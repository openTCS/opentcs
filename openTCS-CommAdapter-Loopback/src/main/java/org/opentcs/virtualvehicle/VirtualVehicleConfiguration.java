/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure to {@link LoopbackCommunicationAdapter}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(VirtualVehicleConfiguration.PREFIX)
public interface VirtualVehicleConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "virtualvehicle";

  @ConfigurationEntry(
      type = "Double",
      description = {"The simulation time factor.",
                     "1.0 is real time, greater values speed up simulation."})
  double simulationTimeFactor();

  @ConfigurationEntry(
      type = "Integer",
      description = "The adapter's command queue capacity.")
  int commandQueueCapacity();

  @ConfigurationEntry(
      type = "String",
      description = "The string to be treated as a recharge operation.")
  String rechargeOperation();

  @ConfigurationEntry(
      type = "Long",
      description = "The maximum allowed size (in bytes) for a profiles description file.")
  long profilesMaxFileSize();
}
