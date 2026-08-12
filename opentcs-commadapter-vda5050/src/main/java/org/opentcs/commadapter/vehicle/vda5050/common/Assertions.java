// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Utility methods for checking preconditions, postconditions etc..
 */
public class Assertions {

  /**
   * Prevent instantiation.
   */
  private Assertions() {
  }

  /**
   * Ensures that {@code value} is not smaller than {@code minimum} and not greater than
   * {@code maximum}.
   *
   * @param value The value to be checked.
   * @param minimum The minimum value.
   * @param maximum The maximum value.
   * @return The given value.
   * @throws IllegalArgumentException If value is not within the given range.
   */
  public static double checkInRange(double value, double minimum, double maximum)
      throws IllegalArgumentException {
    return checkInRange(value, minimum, maximum, "value");
  }

  /**
   * Ensures that {@code value} is not smaller than {@code minimum} and not greater than
   * {@code maximum}.
   *
   * @param value The value to be checked.
   * @param minimum The minimum value.
   * @param maximum The maximum value.
   * @param valueName An optional name for the value to be used for the exception message.
   * @return The given value.
   * @throws IllegalArgumentException If value is not within the given range.
   */
  public static double checkInRange(double value, double minimum, double maximum, String valueName)
      throws IllegalArgumentException {
    if (value < minimum || value > maximum) {
      throw new IllegalArgumentException(
          String.format(
              "%s is not in [%f..%f]: %f",
              String.valueOf(valueName),
              minimum,
              maximum,
              value
          )
      );
    }
    return value;
  }

}
