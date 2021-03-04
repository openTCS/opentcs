/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import java.awt.Image;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * Provides a location theme.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LocationTheme {

  /**
   * Returns the image for the given location representation.
   * 
   * @param representation The representation for which to return the image.
   * @return The image for the given location representation.
   */
  Image getImageFor(LocationRepresentation representation);

  /**
   * Returns a name/short description of this theme.
   * 
   * @return A name/short description of this theme.
   */
  String getName();
}
