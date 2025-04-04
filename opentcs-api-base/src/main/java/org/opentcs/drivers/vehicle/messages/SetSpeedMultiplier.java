// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.messages;

import static org.opentcs.util.Assertions.checkInRange;

import java.io.Serializable;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A message that informs a communication adapter about a speed multiplier it/the vehicle should
 * apply.
 *
 * @deprecated Use communication adapter-specific
 * {@link org.opentcs.drivers.vehicle.VehicleCommAdapterMessage}s instead.
 */
@Deprecated
@ScheduledApiChange(when = "7.0", details = "Will be removed.")
public class SetSpeedMultiplier
    implements
      Serializable {

  /**
   * The speed multiplier in percent.
   */
  private final int multiplier;

  /**
   * Creates a new instance.
   *
   * @param multiplier The speed multiplier in percent.
   */
  public SetSpeedMultiplier(final int multiplier) {
    this.multiplier = checkInRange(multiplier, 0, 100, "multiplier");
  }

  /**
   * Returns the speed multiplier in percent.
   *
   * @return The speed multiplier in percent.
   */
  public int getMultiplier() {
    return multiplier;
  }
}
