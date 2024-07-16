package org.opentcs.customadapter;

import static java.lang.Integer.toHexString;

public record CMD2(int liftHeight, int motion) {
  /**
   * Represents a CMD2 object that encapsulates lift height and motion values.
   * The values must be within valid ranges, otherwise, an IllegalArgumentException is thrown.
   */
  public CMD2 {
    if (liftHeight < 0 || liftHeight > 4095) {  // 4095 is 0xFFF in hexadecimal
      throw new IllegalArgumentException("Invalid lift height");
    }
    if (motion < 0 || motion > 3) {  // Based on the image, motion range is 0-3
      throw new IllegalArgumentException("Invalid motion");
    }
  }

  /**
   * Converts the CMD2 object into a single integer value.
   *
   * @return The integer value representing the CMD2 object.
   */
  public int toInt() {
    return (liftHeight << 4) | motion;
  }

  /**
   * Converts an integer value to a CMD2 object.
   *
   * @param value the integer value to convert
   * @return a CMD2 object representing the converted integer value
   */
  public static CMD2 fromInt(int value) {
    return new CMD2(
        (value >> 4) & 0xFFF,
        value & 0xF
    );
  }

  /**
   * Returns a string representation of the CMD2 object in hexadecimal format.
   *
   * @return A string representation of the CMD2 object in hexadecimal format.
   */
  @Override
  public String toString() {
    String hexLiftHeight = Integer.toHexString(liftHeight).toUpperCase();
    String hexMotion = Integer.toHexString(motion).toUpperCase();
    return hexLiftHeight + hexMotion;
  }
}
