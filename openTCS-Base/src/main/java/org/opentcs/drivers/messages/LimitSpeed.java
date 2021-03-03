/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.messages;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.math.Ranges;

/**
 * A message that informs a communication adapter about a speed limit it/the
 * vehicle should respect.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class LimitSpeed
    implements Serializable {

  /**
   * How the speed value is to be interpreted.
   */
  private final Type type;
  /**
   * The speed limit.
   */
  private final int speed;

  /**
   * Creates a new instance.
   *
   * @param type How the speed value is to be interpreted.
   * @param speed The speed limit.
   */
  public LimitSpeed(final Type type, final int speed) {
    this.type = Objects.requireNonNull(type, "type is null");
    if (!Type.ABSOLUTE.equals(type) && Ranges.outOfRange(speed, 0, 100)) {
      throw new IllegalArgumentException("Relative value not in [0..100]: "
          + speed);
    }
    this.speed = speed;
  }

  /**
   * Returns how the speed value is to be interpreted.
   *
   * @return How the speed value is to be interpreted.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the speed limit.
   *
   * @return The speed limit.
   */
  public int getSpeed() {
    return speed;
  }

  /**
   * Indicates how the speed value is to be interpreted.
   */
  public static enum Type {

    /**
     * Indicates the speed is to be interpreted as an absolute value.
     */
    ABSOLUTE,
    /**
     * Indicates the speed value is to be interpreted as a percentage relative
     * to the maximum speed of the vehicle.
     */
    RELATIVE_VEHICLE,
    /**
     * Indicates the speed value is to be interpreted as a percentage relative
     * to the maximum speed allowed on the paths the vehicle is travelling.
     */
    RELATIVE_PATH
  }
}
