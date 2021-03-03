/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.math;

/**
 * Provides utility methods for working with ranges.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Ranges {
  
  /**
   * Prevents undesired instantiation.
   */
  private Ranges() {
    // Do nada.
  }
  
  /**
   * Checks whether the given value is within the range defined by (and
   * including) the given minimum and maximum.
   *
   * @param value The value to be checked.
   * @param min The minimum value of the range.
   * @param max The maximum value of the range.
   * @return <code>true</code> if, and only if, the given value is within the
   * given range.
   */
  public static boolean inRange(long value, long min, long max) {
    return value >= min && value <= max;
  }
  
  /**
   * Checks whether the given value is outside the range defined by (and
   * including) the given minimum and maximum.
   *
   * @param value The value to be checked.
   * @param min The minimum value of the range.
   * @param max The maximum value of the range.
   * @return <code>true</code> if, and only if, the given value is outside the
   * given range.
   */
  public static boolean outOfRange(long value, long min, long max) {
    return !inRange(value, min, max);
  }
  
}
