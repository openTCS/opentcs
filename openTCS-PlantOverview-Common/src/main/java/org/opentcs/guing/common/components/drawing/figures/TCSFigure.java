/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.AbstractAttributedDecoratedFigure;
import org.jhotdraw.geom.Geom;
import org.opentcs.guing.base.model.DrawnModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.components.drawing.ZoomPoint;

/**
 * Base implementation for figures.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public abstract class TCSFigure
    extends AbstractAttributedDecoratedFigure
    implements ModelBasedFigure {

  /**
   * The enclosing rectangle.
   */
  protected Rectangle fDisplayBox;
  /**
   * The exact position for the middle of the figure.
   */
  protected ZoomPoint fZoomPoint;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The model corresponding to this graphical object.
   */
  public TCSFigure(ModelComponent modelComponent) {
    super();
    set(FigureConstants.MODEL, modelComponent);
  }

  /**
   * Returns the exact point at the middle of the figure.
   *
   * @return the exact point at the middle of the figure.
   */
  public ZoomPoint getZoomPoint() {
    return fZoomPoint;
  }

  /**
   * Sets the zoom point.
   *
   * @param zoomPoint The point at the middle of the figure.
   */
  public void setZoomPoint(ZoomPoint zoomPoint) {
    fZoomPoint = zoomPoint;
  }

  /**
   * Clones this figure, also clones the associated model component.
   *
   * @return
   */
  @Override  // AbstractAttributedDecoratedFigure
  public TCSFigure clone() {
    try {
      TCSFigure that = (TCSFigure) super.clone();
      that.fDisplayBox = new Rectangle(fDisplayBox);
      that.setModel(getModel().clone());

      return that;
    }
    catch (CloneNotSupportedException ex) {
      throw new Error("Cannot clone() unexpectedly", ex);
    }
  }

  @Override
  protected Rectangle2D.Double getFigureDrawingArea() {
    // Add some margin to the drawing area of the figure, so the 
    // drawing area scrolls a little earlier
    Rectangle2D.Double drawingArea = super.getFigureDrawingArea();
    // if we add these two lines the Drawing becomes grey, if we start
    // the application in operating mode..
//    drawingArea.height += 50;
//    drawingArea.width += 100;

    return drawingArea;
  }

  @Override
  public DrawnModelComponent getModel() {
    return (DrawnModelComponent) get(FigureConstants.MODEL);
  }

  public void setModel(ModelComponent model) {
    set(FigureConstants.MODEL, model);
  }

  /**
   * Returns the enclosing rectangle.
   *
   * @return The enclosing rectangle.
   */
  public Rectangle displayBox() {
    return new Rectangle(fDisplayBox);
  }

  @Override
  public boolean figureContains(Point2D.Double p) {
    Rectangle2D.Double r2d = getBounds();
    // Grow for connectors
    Geom.grow(r2d, 10d, 10d);

    return (r2d.contains(p));
  }
}
