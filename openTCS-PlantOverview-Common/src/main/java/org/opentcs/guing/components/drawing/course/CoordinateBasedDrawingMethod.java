/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.course;

/**
 * Eine Zeichenmethode, bei der die Position des Figures und die Realposition in
 * direktem Zusammenhang stehen und sich gegenseitig beeinflussen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CoordinateBasedDrawingMethod
    implements DrawingMethod {

  /**
   * Der Referenzpunkt.
   */
  protected Origin fOrigin;

  /**
   * Creates a new instance of CoordinateBasedDrawingMethod
   */
  public CoordinateBasedDrawingMethod() {
    fOrigin = new Origin();
  }

  @Override
  public Origin getOrigin() {
    return fOrigin;
  }
}
