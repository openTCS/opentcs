/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

/**
 * Defines fixed ordinals for some figures.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface FigureOrdinals {

  /**
   * The layer ordinal to be used for the origin figure.
   * Note: Be cautious with this value. The ordinal for the default layer is 0 . So a value
   * of -1 for the origin figure should be enough. We can't really use e.g. Integer.MIN_VALUE as
   * this leads to unexpected behavior and exceptions when moving layers in the Model Editor
   * application (probably caused by FigureLayerComparator).
   */
  int ORIGIN_FIGURE_ORDINAL = -1;
  /**
   * The layer ordinal to be used for vehicle figures.
   * Note: Be cautious with this value. We want vehicles to be on the uppermost layer. We can't
   * really use e.g. Integer.MAX_VALUE as this leads to unexpected behavior when showing/hiding
   * layers in the Operations Desk application (probably caused by FigureLayerComparator).
   */
  int VEHICLE_FIGURE_ORDINAL = 1000000;
}
