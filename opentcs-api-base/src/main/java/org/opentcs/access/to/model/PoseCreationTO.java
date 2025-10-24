// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * A pose consisting of a position and an orientation angle.
 */
public class PoseCreationTO
    implements
      Serializable {

  /**
   * The position/coordinates in mm.
   */
  private final TripleCreationTO position;
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
  public PoseCreationTO(
      @Nullable
      TripleCreationTO position,
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
  public TripleCreationTO getPosition() {
    return position;
  }

  /**
   * Creates a copy of this object, with the given position.
   *
   * @param position The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public PoseCreationTO withPosition(
      @Nullable
      TripleCreationTO position
  ) {
    return new PoseCreationTO(position, orientationAngle);
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
  public PoseCreationTO withOrientationAngle(double orientationAngle) {
    return new PoseCreationTO(position, orientationAngle);
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
    if (!(obj instanceof PoseCreationTO)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    final PoseCreationTO other = (PoseCreationTO) obj;
    if (Double.doubleToLongBits(this.orientationAngle) != Double.doubleToLongBits(
        other.orientationAngle
    )) {
      return false;
    }
    return Objects.equals(this.position, other.position);
  }

  @Override
  public String toString() {
    return "PoseCreationTO{" + "position=" + position + ", orientationAngle=" + orientationAngle
        + '}';
  }
}
