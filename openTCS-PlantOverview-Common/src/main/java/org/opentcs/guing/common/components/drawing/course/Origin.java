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
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.common.components.drawing.figures.OriginFigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The origin of the coordinate system. Represents the current scale, coordinate system and
 * position of the origin on the screen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public final class Origin {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Origin.class);
  /**
   * Scale (in mm per pixel) of the layout.
   */
  public static final double DEFAULT_SCALE = 50.0;
  /**
   * Amount of mm to equal one pixel on screen in horizontal direction.
   */
  private double fScaleX = DEFAULT_SCALE;
  /**
   * Amount of mm to equal one pixel on screen in horizontal direction.
   */
  private double fScaleY = DEFAULT_SCALE;
  /**
   * Current position in pixels.
   */
  private Point fPosition;
  /**
   * The coordinate system.
   */
  private CoordinateSystem fCoordinateSystem;
  /**
   * List of {@link OriginChangeListener}.
   */
  private final Set<OriginChangeListener> fListeners = new HashSet<>();
  /**
   * Graphical figure to represent the origin.
   */
  private final OriginFigure fFigure = new OriginFigure();

  /**
   * Creates a new instance of Origin
   */
  public Origin() {
    setCoordinateSystem(new NormalCoordinateSystem());
    fFigure.setModel(this);
  }

  /**
   * Set the scale in millimeter per pixel.
   */
  public void setScale(double scaleX, double scaleY) {
    if (fScaleX == scaleX && fScaleY == scaleY) {
      return;
    }
    fScaleX = scaleX;
    fScaleY = scaleY;
    notifyScaleChanged();
  }

  /**
   * Return the millimeter per pixel in horizontal direction.
   *
   * @return the millimeter per pixel in horizontal direction.
   */
  public double getScaleX() {
    return fScaleX;
  }

  /**
   * Return the millimeter per pixel in vertical direction.
   *
   * @return the millimeter per pixel in vertical direction.
   */
  public double getScaleY() {
    return fScaleY;
  }

  /**
   * Set the coordinate system.
   */
  public void setCoordinateSystem(CoordinateSystem coordinateSystem) {
    fCoordinateSystem = coordinateSystem;
    notifyLocationChanged();
  }

  /**
   * Set the position of the origin.
   *
   * @param position the position of the origin.
   */
  public void setPosition(Point position) {
    fPosition = position;
  }

  /**
   * Return the current position of the origin.
   *
   * @return the current position of the origin.
   */
  public Point getPosition() {
    return fPosition;
  }

  /**
   * Translates the real coordinate into pixel coordinates.
   *
   * @param xReal The real x position.
   * @param yReal The real y position.
   * @return A point with the pixel position.
   */
  public Point calculatePixelPosition(LengthProperty xReal, LengthProperty yReal) {
    Point2D pixelExact = calculatePixelPositionExactly(xReal, yReal);

    return new Point((int) pixelExact.getX(), (int) pixelExact.getY());
  }

  /**
   * Translates the real coordinate into pixel coordinates with double precision.
   *
   * @param xReal The real x position.
   * @param yReal The real y position.
   * @return A point with the pixel position with double precision.
   */
  public Point2D calculatePixelPositionExactly(LengthProperty xReal, LengthProperty yReal) {
    Point2D realPosition = new Point2D.Double(
        xReal.getValueByUnit(LengthProperty.Unit.MM),
        yReal.getValueByUnit(LengthProperty.Unit.MM));

    Point2D pixelPosition = fCoordinateSystem.toPixel(fPosition, realPosition, fScaleX, fScaleY);

    return pixelPosition;
  }

  /**
   * Translates the real coordinate into pixel coordinates with double precision from
   * string properties.
   *
   * @param xReal The real x position.
   * @param yReal The real y position.
   * @return A point with the pixel position with double precision.
   */
  public Point2D calculatePixelPositionExactly(StringProperty xReal, StringProperty yReal) {
    try {
      double xPos = Double.parseDouble(xReal.getText());
      double yPos = Double.parseDouble(yReal.getText());
      Point2D realPosition = new Point2D.Double(xPos, yPos);
      Point2D pixelPosition = fCoordinateSystem.toPixel(fPosition, realPosition, fScaleX, fScaleY);

      return pixelPosition;
    }
    catch (NumberFormatException e) {
      LOG.info("Couldn't parse layout coordinates", e);
      return new Point2D.Double();
    }
  }

  /**
   * Translates a pixel position into a real position and write to the length properties.
   *
   *
   * @param pixelPosition The pixel position to convert.
   * @param xReal The length property to write the x position to.
   * @param yReal The length property to write the y position to.
   * @return A point with the pixel position with double precision.
   */
  public Point2D calculateRealPosition(Point pixelPosition, LengthProperty xReal,
                                       LengthProperty yReal) {
    Point2D realPosition = fCoordinateSystem.toReal(fPosition, pixelPosition, fScaleX, fScaleY);

    LengthProperty.Unit unitX = xReal.getUnit();
    LengthProperty.Unit unitY = yReal.getUnit();

    xReal.setValueAndUnit((int) realPosition.getX(), LengthProperty.Unit.MM);
    yReal.setValueAndUnit((int) realPosition.getY(), LengthProperty.Unit.MM);
    xReal.convertTo(unitX);
    yReal.convertTo(unitY);

    return realPosition;
  }

  /**
   * Translates a pixel position onto a real position.
   *
   * @param pixelPosition The pixel position to convert.
   * @return A point with the pixel position with double precision.
   */
  public Point2D calculateRealPosition(Point pixelPosition) {
    Point2D realPosition = fCoordinateSystem.toReal(fPosition, pixelPosition, fScaleX, fScaleY);

    return realPosition;
  }

  /**
   * Add an origin change listener.
   *
   * @param l The origin change listener to add.
   */
  public void addListener(OriginChangeListener l) {
    fListeners.add(l);
  }

  /**
   * Remove an origin change listener.
   *
   * @param l The origin change listener to remove.
   */
  public void removeListener(OriginChangeListener l) {
    fListeners.remove(l);
  }

  /**
   *
   * Tests whether a specific origin change listener is registerd.
   *
   * @param l The origin change listener to test for.
   * @return <code> true </code>, if the listener is registerd.
   */
  public boolean containsListener(OriginChangeListener l) {
    return fListeners.contains(l);
  }

  /**
   * Notifies all registered listeners that the position of the origin has changed.
   */
  public void notifyLocationChanged() {
    for (OriginChangeListener l : fListeners) {
      l.originLocationChanged(new EventObject(this));
    }
  }

  /**
   * Notifies all registered listeners that the scale has changed.
   */
  public void notifyScaleChanged() {
    for (OriginChangeListener l : fListeners) {
      l.originScaleChanged(new EventObject(this));
    }
  }

  /**
   * Return the graphical representation of the origin.
   *
   * @return The graphical representation of the origin.
   */
  public OriginFigure getFigure() {
    return fFigure;
  }
}
