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

/**
 * A coordinate system strategy that converts pixel coordinates into real coordinates.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class NormalCoordinateSystem
    implements CoordinateSystem {

  /**
   * Creates a new instance.
   */
  public NormalCoordinateSystem() {
  }

  @Override
  public Point2D toPixel(Point refPointLocation, Point2D realValue, double scaleX, double scaleY) {
    double xPixel = realValue.getX() / scaleX;
    double yPixel = realValue.getY() / scaleY;
    return new Point2D.Double(refPointLocation.x + xPixel, -(refPointLocation.y + yPixel));
  }

  @Override
  public Point2D toReal(Point refPointLocation, Point pixelValue, double scaleX, double scaleY) {
    int xDiff = pixelValue.x - refPointLocation.x;
    int yDiff = pixelValue.y - refPointLocation.y;
    return new Point2D.Double(scaleX * xDiff, -scaleY * yDiff);
  }
}
