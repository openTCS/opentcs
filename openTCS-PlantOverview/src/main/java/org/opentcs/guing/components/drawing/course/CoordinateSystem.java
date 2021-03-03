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
import java.io.Serializable;

/**
 * Eine Strategie mit der Pixelkoordinaten in tatsächliche Koordinaten
 * umgewandelt werden können. Normiert wird auf mm.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface CoordinateSystem
    extends Serializable {

  /**
   * Wandelt echte Koordinaten in Pixelkoordinaten um. Die echten Koordinaten
   * gibt der Benutzer durch Ändern der Attribute vor. Daraufhin muss berechnet
   * werden, an welche Position in Pixel das entsprechende Figure zu setzen ist.
   *
   * @param refPointLocation Die aktuelle Position des Referenzpunktes
   * @param realValue Der reale Längenwert, beispielsweise 100 mm.
   * @param relationX Soviel mm entsprechen einem Pixel in x-Richtung.
   * @param relationY Soviel mm entsprechen einem Pixel in y-Richtung.
   * @return Der Pixelwert ungerundet.
   */
  Point2D toPixel(Point refPointLocation, Point2D realValue, double relationX, double relationY);

  /**
   * Wandelt Pixelkoordinaten in echte Koordinaten um. Der Benutzer verschiebt
   * ein Figure. Diese neue Position in Pixel muss nun in reale Koordinaten
   * umgerechnet werden.
   *
   * @param refPointLocation Die aktuelle Position des Referenzpunktes in Pixel.
   * @param pixelValue Der Wert in Pixel.
   * @param relationX Soviel mm entsprechen einem Pixel in x-Richtung.
   * @param relationY Soviel mm entsprechen einem Pixel in y-Richtung.
   * @return Der Realwert in mm.
   */
  Point2D toReal(Point refPointLocation, Point pixelValue, double relationX, double relationY);
}
