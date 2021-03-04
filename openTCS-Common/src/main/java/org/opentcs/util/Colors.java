/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import java.awt.Color;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * Provides utilty methods for working with colors.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Colors {

  /**
   * Prevents instantiation of this utility class.
   */
  private Colors() {
  }

  /**
   * Returns a hexadecimal representation of the given color in the RGB color space.
   * <p>
   * The pattern of the strings returned by this method is {@code "#RRGGBB"}.
   * </p>
   *
   * @param color The color to be encoded.
   * @return The representation of the given color.
   */
  @Nonnull
  public static String encodeToHexRGB(@Nonnull Color color) {
    requireNonNull(color, "color");

    return String.format("#%06X", color.getRGB() & 0x00FFFFFF);
  }

  /**
   * Returns a {@code Color} instance described by the given hexadecimal representation.
   *
   * @param rgbHex The hexadecimal representation of the color to be returned in the RGB color
   * space.
   *
   * @return A {@code Color} instance described by the given value.
   * @throws NumberFormatException If the given string cannot be parsed.
   */
  @Nonnull
  public static Color decodeFromHexRGB(@Nonnull String rgbHex)
      throws NumberFormatException {
    requireNonNull(rgbHex, "rgbHex");

    return Color.decode(rgbHex);
  }

}
