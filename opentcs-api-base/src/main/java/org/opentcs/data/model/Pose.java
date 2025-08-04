// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.model;

import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * A pose consisting of a position and an orientation angle.
 */
public class Pose
    implements
      Serializable {

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
  public Pose(
      @Nullable
      Triple position,
      double orientationAngle
  ) {
    this.position = position;
    checkArgument(
        Double.isNaN(orientationAngle)
            || (orientationAngle >= -360.0 && orientationAngle <= 360.0),
        "orientationAngle not Double.NaN or in [-360..360]: %s",
        orientationAngle
    );
    this.orientationAngle = orientationAngle;
  }

  /**
   * The position/coordinates in mm.
   *
   * @return The position/coordinates in mm.
   */
  @Nullable
  public Triple getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Pose withPosition(
      @Nullable
      Triple position
  ) {
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
    return Objects.hash(position, orientationAngle);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Pose other)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (Double.doubleToLongBits(this.orientationAngle) != Double.doubleToLongBits(
        other.orientationAngle
    )) {
      return false;
    }
    return Objects.equals(this.position, other.position);
  }

  @Override
  public String toString() {
    return "Pose{" + "position=" + position + ", orientationAngle=" + orientationAngle + '}';
  }
}
