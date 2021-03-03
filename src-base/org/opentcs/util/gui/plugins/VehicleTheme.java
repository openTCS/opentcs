/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.plugins;

import java.util.Set;
import org.opentcs.data.model.Vehicle;

/**
 * Provides a vehicle theme.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public interface VehicleTheme {

  /**
   * Returns the path to the image for the given vehicle.
   *
   * @param vehicle The vehicle for which to return the path.
   * @return The path to the image for the given location representation.
   */
  String getImagePathFor(Vehicle vehicle);

  /**
   * Returns a name/short description of this theme.
   *
   * @return A name/short description of this theme.
   */
  String getName();
  
  /**
   * Returns all image paths used by this theme. If a path to an image is 
   * missing the image will not be loaded and therefore not be available.
   * 
   * @return Set containing all image paths.
   */
  Set<String> getAllImagePaths();
}
