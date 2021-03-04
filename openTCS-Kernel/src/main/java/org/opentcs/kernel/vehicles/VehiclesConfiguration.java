/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import org.opentcs.util.configuration.ConfigurationEntry;
import org.opentcs.util.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link DefaultVehicleController}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@ConfigurationPrefix(VehiclesConfiguration.PREFIX)
public interface VehiclesConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "vehicles";

  @ConfigurationEntry(
      type = "Boolean",
      description = {
        "Whether to ignore unknown positions reported by a vehicle.",
        "If not ignored, unknown positions reset the vehicle's position in the course model."
      })
  boolean ignoreUnknownReportedPositions();
}
