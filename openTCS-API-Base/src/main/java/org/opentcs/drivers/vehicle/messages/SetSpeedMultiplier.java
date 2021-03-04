/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.messages;

import java.io.Serializable;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * A message that informs a communication adapter about a speed multiplier it/the vehicle should
 * apply.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SetSpeedMultiplier
    implements Serializable {

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
