/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Provides a vehicle theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public interface VehicleTheme {

  /**
   * Returns an image for the given vehicle, representing its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   * @deprecated Use {@link #statefulImage(org.opentcs.data.model.Vehicle)}
   * or {@link #statelessImage(org.opentcs.data.model.Vehicle)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  Image getImageFor(Vehicle vehicle);

  /**
   * Returns an image for the given vehicle, disregarding its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default Image statelessImage(@Nonnull Vehicle vehicle) {
    return getImageFor(vehicle);
  }

  /**
   * Returns an image for the given vehicle, representing its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default Image statefulImage(@Nonnull Vehicle vehicle) {
    return getImageFor(vehicle);
  }

  /**
   * Returns an image representing this theme, usually an image of the vehicle
   * in its normal state.
   *
   * @return A default image for this theme.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  Image getThemeImage();

  /**
   * Returns a name/short description of this theme.
   *
   * @return A name/short description of this theme.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  String getName();

  /**
   * Provides a label that describes this vehicle.
   * Usually this is the name of the vehicle or an abbreviation.
   *
   * @param vehicle The vehicle to provide a label for.
   * @return A label that describes the given vehicle.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default String label(Vehicle vehicle) {
    String name = vehicle.getName();
    // Find digits.
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher(name);

    // If at least one group of digits was found, use the first one.
    if (m.find()) {
      return m.group();
    }
    return name;
  }

  /**
   * Provides the vertical offset of the label relative to the center of the vehicle figure.
   *
   * @return The horizontal offset.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default int labelOffsetX() {
    return -8;
  }

  /**
   * Provides the vertical offset of the label relative to the center of the vehicle figure.
   *
   * @return The vertical offset.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default int labelOffsetY() {
    return 5;
  }

  /**
   * Provides the color to be used for drawing the label.
   *
   * @return The color to be used for drawing the label.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default Color labelColor() {
    return Color.BLUE;
  }

  /**
   * Provides the font to be used for drawing the label.
   *
   * @return The font to be used for drawing the label.
   */
  @ScheduledApiChange(when = "5.0", details = "Default implementation will be removed.")
  default Font labelFont() {
    return new Font("Arial", Font.BOLD, 12);
  }
}
