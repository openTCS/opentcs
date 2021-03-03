/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

/**
 * Provides methods for optimized parsing of numbers from character sequences.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class NumberParsers {
  /**
   * The number of characters used for <code>Long.MAX_VALUE</code>.
   */
  private static final int maxPositiveLongChars =
        Long.toString(Long.MAX_VALUE).length();
  /**
   * The number of characters used for <code>Long.MIN_VALUE</code>.
   */
  private static final int maxNegativeLongChars =
        Long.toString(Long.MIN_VALUE).length();
  
  /**
   * Prevents creation of instances of this class.
   */
  private NumberParsers() {
  }
  
  /**
   * Parses a sequence of characters as a decimal number and returns the latter.
   *
   * @param source The character sequence to be parsed.
   * @return The decimal number represented by the given character sequence.
   * @throws NumberFormatException If the parsed sequence of characters does not
   * represent a decimal number.
   */
  public static long parsePureDecimalLong(CharSequence source)
  throws NumberFormatException {
    return parsePureDecimalLong(source, 0, source.length());
  }
  
  /**
   * Parses a (sub)sequence of characters as a decimal number and returns the
   * latter.
   *
   * @param source The character sequence to be parsed.
   * @param startIndex The position at which to start parsing.
   * @param length The number of characters to parse.
   * @return The decimal number represented by the given character sequence.
   * @throws NumberFormatException If the parsed sequence of characters does not
   * represent a decimal number.
   */
  public static long parsePureDecimalLong(CharSequence source, int startIndex,
        int length)
  throws NumberFormatException {
    if (source == null) {
      throw new NullPointerException("source is null");
    }
    if (startIndex < 0 || startIndex > source.length() - 1) {
      throw new IllegalArgumentException("startIndex out of bounds");
    }
    if (length < 1) {
      throw new IllegalArgumentException("length is not a positive value");
    }
    long result = 0;
    int index = 0;
    boolean negative;
    long limit;
    // Check if we have a negative number and initialize accordingly.
    if (source.charAt(startIndex) == '-') {
      if (length > maxNegativeLongChars) {
        throw new NumberFormatException("too long to be parsed");
      }
      negative = true;
      limit = Long.MIN_VALUE;
      index++;
    }
    else {
      if (length > maxPositiveLongChars) {
        throw new NumberFormatException("too long to be parsed");
      }
      negative = false;
      limit = -Long.MAX_VALUE;
    }
    while (index < length) {
      int digit = source.charAt(startIndex + index) - '0';
      // If we've just read something other than a digit, throw an exception.
      if (digit < 0 || digit > 9) {
        throw new NumberFormatException(
              "not a decimal digit: " + source.charAt(startIndex + index));
      }
      result *= 10;
      // Check if the next operation would overflow the result.
      if (result < limit + digit) {
        throw new NumberFormatException(
              "parsed number exceeds value boundaries");
      }
      result -= digit;
      index++;
    }
    if (negative) {
      // If we did not parse at least one digit, throw an exception.
      if (index < 2) {
        throw new NumberFormatException("minus sign without succeeding digits");
      }
      return result;
    }
    else {
      return -result;
    }
  }
}
