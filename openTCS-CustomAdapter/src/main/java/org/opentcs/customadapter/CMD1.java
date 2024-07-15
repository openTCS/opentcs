package org.opentcs.customadapter;

public record CMD1(int liftCmd, int speed, int obstacleSensor, int direction) {
  /**
   * Validates and constructs a CMD1 object.
   *
   * @param liftCmd The lift command. Must be in the range of 0 to 2 (inclusive).
   * @param speed The speed. Must be in the range of 1 to 5 (inclusive).
   * @param obstacleSensor The obstacle sensor. Must be in the range of 1 to 15 (inclusive).
   * @param direction The direction. Must be either 0 or 1.
   * @throws IllegalArgumentException If any of the parameters are invalid.
   */
  public CMD1 {
    if (liftCmd < 0 || liftCmd > 2) {
      throw new IllegalArgumentException("Invalid liftCmd");
    }
    if (speed < 1 || speed > 5) {
      throw new IllegalArgumentException("Invalid speed");
    }
    if (obstacleSensor < 1 || obstacleSensor > 15) {
      throw new IllegalArgumentException(
          "Invalid obstacleSensor"
      );
    }
    if (direction < 0 || direction > 1) {
      throw new IllegalArgumentException("Invalid direction");
    }
  }

  /**
   * Converts the CMD1 object to an integer representation.
   *
   * @return The integer representation of the CMD1 object.
   */
  public int toInt() {
    return (liftCmd << 12) | (speed << 8) | (obstacleSensor << 4) | direction;
  }

  /**
   * Converts an integer value to a CMD1 object.
   *
   * @param value The integer value to convert.
   * @return A CMD1 object constructed from the integer value.
   */
  public static CMD1 fromInt(int value) {
    return new CMD1(
        (value >> 12) & 0xF,
        (value >> 8) & 0xF,
        (value >> 4) & 0xF,
        value & 0xF
    );
  }
}
