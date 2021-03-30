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
import javax.annotation.Nonnull;
import org.opentcs.data.model.Vehicle;

/**
 * Provides a vehicle theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public interface VehicleTheme {

  /**
   * Returns an image for the given vehicle, disregarding its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   */
  Image statelessImage(@Nonnull Vehicle vehicle);

  /**
   * Returns an image for the given vehicle, representing its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   */
  Image statefulImage(@Nonnull Vehicle vehicle);

  /**
   * Provides a label that describes this vehicle.
   * Usually this is the name of the vehicle or an abbreviation.
   *
   * @param vehicle The vehicle to provide a label for.
   * @return A label that describes the given vehicle.
   */
  String label(Vehicle vehicle);

  /**
   * Provides the vertical offset of the label relative to the center of the vehicle figure.
   *
   * @return The horizontal offset.
   */
  int labelOffsetX();

  /**
   * Provides the vertical offset of the label relative to the center of the vehicle figure.
   *
   * @return The vertical offset.
   */
  int labelOffsetY();

  /**
   * Provides the color to be used for drawing the label.
   *
   * @return The color to be used for drawing the label.
   */
  Color labelColor();

  /**
   * Provides the font to be used for drawing the label.
   *
   * @return The font to be used for drawing the label.
   */
  Font labelFont();
}
