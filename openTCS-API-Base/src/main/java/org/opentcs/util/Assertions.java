/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import javax.annotation.Nullable;

/**
 * Utility methods for checking preconditions, postconditions etc..
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Assertions {

  /**
   * Prevents instatiation.
   */
  private Assertions() {
  }

  /**
   * Ensures the given expression is {@code true}.
   *
   * @param expression The expression to be checked.
   * @param errorMessage An optional error message.
   * @throws IllegalArgumentException If the given expression is not true.
   */
  public static void checkArgument(boolean expression, String errorMessage)
      throws IllegalArgumentException {
    checkArgument(expression, errorMessage, (Object) null);
  }

  /**
   * Ensures the given expression is {@code true}.
   *
   * @param expression The expression to be checked.
   * @param messageTemplate A formatting template for the error message.
   * @param messageArgs The arguments to be formatted into the message template.
   * @throws IllegalArgumentException If the given expression is not true.
   */
  public static void checkArgument(boolean expression,
                                   String messageTemplate,
                                   @Nullable Object... messageArgs)
      throws IllegalArgumentException {
    if (!expression) {
      throw new IllegalArgumentException(String.format(String.valueOf(messageTemplate),
                                                       messageArgs));
    }
  }

  /**
   * Ensures the given expression is {@code true}.
   *
   * @param expression The expression to be checked.
   * @param errorMessage An optional error message.
   * @throws IllegalStateException If the given expression is not true.
   */
  public static void checkState(boolean expression, String errorMessage)
      throws IllegalStateException {
    checkState(expression, errorMessage, (Object) null);
  }

  /**
   * Ensures the given expression is {@code true}.
   *
   * @param expression The expression to be checked.
   * @param messageTemplate A formatting template for the error message.
   * @param messageArgs The arguments to be formatted into the message template.
   * @throws IllegalStateException If the given expression is not true.
   */
  public static void checkState(boolean expression,
                                String messageTemplate,
                                @Nullable Object... messageArgs)
      throws IllegalStateException {
    if (!expression) {
      throw new IllegalStateException(String.format(String.valueOf(messageTemplate),
                                                    messageArgs));
    }
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
  public static int checkInRange(int value, int minimum, int maximum)
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
  public static int checkInRange(int value, int minimum, int maximum, String valueName)
      throws IllegalArgumentException {
    if (value < minimum || value > maximum) {
      throw new IllegalArgumentException(String.format("%s is not in [%d..%d]: %d",
                                                       String.valueOf(valueName),
                                                       minimum,
                                                       maximum,
                                                       value));
    }
    return value;
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
  public static long checkInRange(long value, long minimum, long maximum)
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
  public static long checkInRange(long value, long minimum, long maximum, String valueName)
      throws IllegalArgumentException {
    if (value < minimum || value > maximum) {
      throw new IllegalArgumentException(String.format("%s is not in [%d..%d]: %d",
                                                       String.valueOf(valueName),
                                                       minimum,
                                                       maximum,
                                                       value));
    }
    return value;
  }
}
