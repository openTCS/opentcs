/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.course;

/**
 * A drawing method where the position of a figure and the real position are in relation
 * to each other.
 */
public class CoordinateBasedDrawingMethod
    implements DrawingMethod {

  /**
   * The origin point.
   */
  protected Origin fOrigin;

  /**
   * Creates a new instance.
   */
  public CoordinateBasedDrawingMethod() {
    fOrigin = new Origin();
  }

  @Override
  public Origin getOrigin() {
    return fOrigin;
  }
}
