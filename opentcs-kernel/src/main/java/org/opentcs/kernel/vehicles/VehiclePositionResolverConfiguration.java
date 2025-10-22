// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Configuration for {@link VehiclePositionResolver}.
 */
@ConfigurationPrefix(VehiclePositionResolverConfiguration.PREFIX)
public interface VehiclePositionResolverConfiguration {
  /**
   * This configuration's prefix.
   */
  String PREFIX = "vehiclepositionresolver";

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum allowed deviation of the x and y-coordinates between a logical"
          + " position and the vehicle's actual precise position.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_vehiclepositionresolver_0"
  )
  int deviationXY();

  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum allowed deviation of the orientation angle between a logical"
          + " position and the vehicle's actual precise position.",
      changesApplied = ConfigurationEntry.ChangesApplied.INSTANTLY,
      orderKey = "1_vehiclepositionresolver_1"
  )
  int deviationTheta();
}
