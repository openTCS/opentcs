/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.awt.Image;
import org.opentcs.data.model.Vehicle;

/**
 * Provides a vehicle theme.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleTheme {

  /**
   * Returns an image for the given vehicle, representing its current state.
   *
   * @param vehicle The vehicle for which to return the image.
   * @return An image for the given vehicle.
   */
  Image getImageFor(Vehicle vehicle);

  /**
   * Returns an image representing this theme, usually an image of the vehicle
   * in its normal state.
   *
   * @return A default image for this theme.
   */
  Image getThemeImage();

  /**
   * Returns a name/short description of this theme.
   *
   * @return A name/short description of this theme.
   */
  String getName();
}
