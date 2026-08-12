// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Mathematical functions dealing with angles.
 */
public class AngleMath {

  private AngleMath() {
  }

  /**
   * Returns a convex angle that is equivalent to the given angle and relative to 0 degrees.
   * <p>
   * The result is an angle between -180 and 180 degrees.
   * Angles smaller than -180 degrees or larger than 180 degrees are mapped to the respective
   * equivalent convex angle.
   * For instance, an angle of -270 degrees will be mapped to 90 degrees, and an angle of 181
   * degrees will be mapped to -179 degrees.
   * </p>
   *
   * @param degrees The angle to be mapped
   * @return A convex angle that is equivalent to the given angle and relative to 0 degrees.
   */
  public static double toRelativeConvexAngle(double degrees) {
    double result = degrees % 360.0;
    if (result > 180.0) {
      result -= 360.0;
    }
    else if (result < -180.0) {
      result += 360.0;
    }
    return result;
  }

  /**
   * Returns the absolute difference between two angles (in the range from 0 to 180 degrees).
   *
   * @param angle1 The first angle, in degrees.
   * @param angle2 The second angle, in degrees.
   * @return The difference.
   */
  public static double angleBetween(double angle1, double angle2) {
    double difference = Math.abs(angle1 - angle2) % 360;
    if (difference > 180) {
      return Math.abs(difference - 360);
    }
    return difference;
  }
}
