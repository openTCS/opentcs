// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.course;

/**
 * A drawing method where the position of a figure and the real position are in relation
 * to each other.
 */
public class CoordinateBasedDrawingMethod
    implements
      DrawingMethod {

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
