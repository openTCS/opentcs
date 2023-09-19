/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A pose consisting of a position and an orientation angle.
 */
public class Pose
    implements Serializable {

  /**
   * The position/coordinates in mm.
   */
  private final Triple position;
  /**
   * The orientation angle in degrees (-360..360). May be Double.NaN if unknown/undefined.
   */
  private final double orientationAngle;

  /**
   * Creates a new instance.
   *
   * @param position The position/coordinates in mm.
   * @param orientationAngle The orientation angle in degrees (-360..360). May be Double.NaN if
   * unknown/undefined.
   */
  public Pose(@Nonnull Triple position, double orientationAngle) {
    this.position = requireNonNull(position, "position");
    checkArgument(Double.isNaN(orientationAngle)
        || (orientationAngle >= -360.0 && orientationAngle <= 360.0),
                  "orientationAngle not Double.NaN or in [-360..360]: %s",
                  orientationAngle);
    this.orientationAngle = orientationAngle;
  }

  /**
   * The position/coordinates in mm.
   *
   * @return The position/coordinates in mm.
   */
  @Nonnull
  public Triple getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Pose withPosition(@Nonnull Triple position) {
    return new Pose(position, orientationAngle);
  }

  /**
   * The orientation angle in degrees (-360..360). May be Double.NaN if unknown/undefined.
   *
   * @return The orientation angle in degrees, or Double.NaN.
   */
  public double getOrientationAngle() {
    return orientationAngle;
  }

  /**
   * Creates a copy of this object, with the given orientation angle.
   *
   * @param orientationAngle The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Pose withOrientationAngle(double orientationAngle) {
    return new Pose(position, orientationAngle);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 47 * hash + Objects.hashCode(this.position);
    hash = 47 * hash
        + (int) (Double.doubleToLongBits(this.orientationAngle)
                 ^ (Double.doubleToLongBits(this.orientationAngle) >>> 32));
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Pose)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    final Pose other = (Pose) obj;
    if (Double.doubleToLongBits(this.orientationAngle)
        != Double.doubleToLongBits(other.orientationAngle)) {
      return false;
    }
    return Objects.equals(this.position, other.position);
  }

  @Override
  public String toString() {
    return "Pose{" + "position=" + position + ", orientationAngle=" + orientationAngle + '}';
  }
}
