package org.opentcs.customadapter;

public record CMD2(int horizontal, int guideTimeout, int motion) {
  /**
   * Represents a CMD2 object that encapsulates horizontal, guideTimeout, and motion values.
   * The values must be within valid ranges, otherwise, an IllegalArgumentException is thrown.
   */
  public CMD2 {
    if (horizontal < 0 || horizontal > 255) {
      throw new IllegalArgumentException("Invalid horizontal");
    }
    if (guideTimeout < 0 || guideTimeout > 5) {
      throw new IllegalArgumentException("Invalid guideTimeout");
    }
    if (motion < 0 || motion > 6) {
      throw new IllegalArgumentException("Invalid motion");
    }
  }

  /**
   * Converts the CMD2 object into a single integer value.
   *
   * @return The integer value representing the CMD2 object.
   */
  public int toInt() {
    return (horizontal << 8) | (guideTimeout << 4) | motion;
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
        (value >> 4) & 0xF,
        value & 0xF
    );
  }
}
