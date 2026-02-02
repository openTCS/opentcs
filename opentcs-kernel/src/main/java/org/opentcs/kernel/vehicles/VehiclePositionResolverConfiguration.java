// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Configuration for {@link VehiclePositionResolver}.
 */
@ConfigurationPrefix(VehiclePositionResolverConfiguration.PREFIX)
public interface VehiclePositionResolverConfiguration {
  /**
   * This configuration's prefix.
   */
  @ScheduledApiChange(
      when = "8.0",
      details = "Will be renamed to prefer naming it after the feature, not the component."
  )
  String PREFIX = "vehiclepositionresolver";

  @ScheduledApiChange(
      when = "8.0",
      details = "Will be renamed to reflect it's the default policy's deviation distance."
  )
  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum allowed deviation of the x and y-coordinates between a logical"
          + " position and the vehicle's actual precise position, in millimeters.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_vehiclepositionresolver_0"
  )
  int deviationXY();

  @ScheduledApiChange(
      when = "8.0",
      details = "Will be renamed to reflect it's the default policy's deviation angle."
  )
  @ConfigurationEntry(
      type = "Integer",
      description = "The maximum allowed deviation of the orientation angle between a logical"
          + " position and the vehicle's actual precise position, in degrees.",
      changesApplied = ConfigurationEntry.ChangesApplied.ON_NEW_PLANT_MODEL,
      orderKey = "1_vehiclepositionresolver_1"
  )
  int deviationTheta();
}
