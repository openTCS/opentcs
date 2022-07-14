/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.course;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * A strategy that can translate pixel coordinates to real coordinates.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface CoordinateSystem
    extends Serializable {

  /**
   * Translates the real coordinate into a pixel coordinate.
   *
   * @param refPointLocation The current position of the reference point.
   * @param realValue The real position to translate.
   * @param relationX The amount of mm for one pixel in the x axis.
   * @param relationY The amount of mm for one pixel in the y axis.
   * @return A point with the pixel coordinates.
   */
  Point2D toPixel(Point refPointLocation, Point2D realValue, double relationX, double relationY);

  /**
   * Translates the pixel coordinate into a real coordinate.
   *
   * @param refPointLocation The current position of the reference point.
   * @param pixelValue The pixel coordinate position to translate.
   * @param relationX The amount of mm for one pixel in the x axis.
   * @param relationY The amount of mm for one pixel in the y axis.
   * @return A point with the real position.
   */
  Point2D toReal(Point refPointLocation, Point pixelValue, double relationX, double relationY);
}
