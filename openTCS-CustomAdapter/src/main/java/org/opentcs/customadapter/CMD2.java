package org.opentcs.customadapter;

public record CMD2(int liftHeight, int motion, int station) {
  /**
   * Represents a CMD2 object that encapsulates lift height and motion values.
   * The values must be within valid ranges, otherwise, an IllegalArgumentException is thrown.
   */
  public CMD2 {
    if (liftHeight < 0 || liftHeight > 255) {  // 4095 is 0xFFF in hexadecimal
      throw new IllegalArgumentException("Invalid lift height");
    }
    if (motion < 0 || motion > 3) {  // Based on the image, motion range is 0-3
      throw new IllegalArgumentException("Invalid motion");
    }
    if (station < 0 || station > 4) {  // 15 is 0xF in hexadecimal
      throw new IllegalArgumentException("Invalid station");
    }
  }

  /**
   * Converts the CMD2 object into a single integer value.
   *
   * @return The integer value representing the CMD2 object.
   */
  public int toInt() {
    return (liftHeight << 8) | (station << 4) | motion;
  }

  /**
   * Converts an integer value to a CMD2 object.
   *
   * @param value the integer value to convert
   * @return a CMD2 object representing the converted integer value
   */
  public static CMD2 fromInt(int value) {
    return new CMD2(
        (value >> 8) & 0xFF,
        value & 0xF,
        (value >> 4) & 0xF
    );
  }

  /**
   * Returns a string representation of the CMD2 object in hexadecimal format.
   *
   * @return A string representation of the CMD2 object in hexadecimal format.
   */
  @Override
  public String toString() {
    String hexLiftHeight = String.format("%02X", liftHeight);
    String hexStation = Integer.toHexString(station).toUpperCase();
    String hexMotion = Integer.toHexString(motion).toUpperCase();
    return hexLiftHeight + hexStation + hexMotion;
  }

  /**
   * Converts the CMD2 object to a 16-bit short value.
   *
   * @return The short value representing the CMD2 object.
   */
  public short toShort() {
    int combinedValue = toInt();
    return (short) combinedValue;
  }
}
