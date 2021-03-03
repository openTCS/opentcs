/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.gui.plugins;

import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 * Provides a location theme.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public interface LocationTheme {

  /**
   * Returns the path to the image for the given location representation.
   * 
   * @param representation The representation for which to return the path.
   * @return The path to the image for the given location representation.
   */
  String getImagePathFor(LocationRepresentation representation);

  /**
   * Returns a name/short description of this theme.
   * 
   * @return A name/short description of this theme.
   */
  String getName();
}
