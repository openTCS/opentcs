/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Represents an exact point that won't change when zooming the model.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class ZoomPoint
    implements Serializable {

  /**
   * The x position with a scale of 1.
   */
  protected double fX;
  /**
   * The y position with a scale of 1.
   */
  protected double fY;
  /**
   * The current scale.
   */
  protected double fScale;

  /**
   * Creates a new instance of ZoomPoint
   */
  public ZoomPoint() {
    this(0, 0);
  }

  /**
   * Creates a new instance.
   *
   * @param x The x position for this point.
   * @param y The y position for this point.
   */
  public ZoomPoint(double x, double y) {
    this(x, y, 1.0);
  }

  /**
   * Creates a new instance.
   *
   * @param x The x position for this point.
   * @param y The y position for this point.
   * @param scale The current scale.
   */
  public ZoomPoint(double x, double y, double scale) {
    fX = x / scale;
    fY = y / scale;
    fScale = scale;
  }

  /**
   * Returns the current scale.
   */
  public double scale() {
    return fScale;
  }

  /**
   * Sets the x position.
   *
   * @param x the x position.
   */
  public void setX(double x) {
    fX = x;
  }

  /**
   * Sets the y position.
   *
   * @param y the y position.
   */
  public void setY(double y) {
    fY = y;
  }

  /**
   * Returns the x position.
   *
   * @return the x position.
   */
  public double getX() {
    return fX;
  }

  /**
   * Returns the y position.
   *
   * @return the y position.
   */
  public double getY() {
    return fY;
  }

  /**
   * Returns a point with the position.
   *
   * @return a point with the position.
   */
  public Point getPixelLocation() {
    int x = (int) (getX() * scale());
    int y = (int) (getY() * scale());

    return new Point(x, y);
  }

  /**
   * Returns the exact position of the point in pixels with the current zoom level.
   *
   * @return the exact position.
   */
  public Point2D getPixelLocationExactly() {
    double x = getX() * scale();
    double y = getY() * scale();

    return new Point2D.Double(x, y);
  }

  /**
   * Event that the scale has changed.
   *
   * @param scale The new scale factor.
   */
  public void scaleChanged(double scale) {
    fScale = scale;
  }

  /**
   * Event that the point has been moved by the user.
   *
   * @param x The x-coordinate in Pixel.
   * @param y The y-coordinate in Pixel.
   */
  public void movedByMouse(int x, int y) {
    fX = x / scale();
    fY = y / scale();
  }
}
