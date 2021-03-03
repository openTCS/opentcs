/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.course;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Eine Strategie mit der Pixelkoordinaten in tatsächliche Koordinaten
 * umgewandelt werden können. Repräsentiert ein normales Koordinatensystem, bei
 * dem der erste Quadrant in der rechten oberen Ecke liegt. Wichtig ist bei der
 * Umrechnung der aktuelle Maßstab (Zoomfaktor) sowie die Position des
 * Referenzpunktes.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class NormalCoordinateSystem
    implements CoordinateSystem {

  /**
   * Creates a new instance of StandardPositionStrategy
   */
  public NormalCoordinateSystem() {
  }

  @Override
  public Point2D toPixel(Point refPointLocation, Point2D realValue, double scaleX, double scaleY) {
    double xPixel = realValue.getX() / scaleX;
    double yPixel = realValue.getY() / scaleY;
    // Vorzeichen für y negativ (y-Achse zeigt nach oben)
    return new Point2D.Double(refPointLocation.x + xPixel, -(refPointLocation.y + yPixel));
  }

  @Override
  public Point2D toReal(Point refPointLocation, Point pixelValue, double scaleX, double scaleY) {
    int xDiff = pixelValue.x - refPointLocation.x;
    int yDiff = pixelValue.y - refPointLocation.y;
    // Vorzeichen für y negativ (y-Achse zeigt nach oben)
    return new Point2D.Double(scaleX * xDiff, -scaleY * yDiff);
  }
}
