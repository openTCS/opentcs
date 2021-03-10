/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.AbstractAttributedDecoratedFigure;
import org.jhotdraw.geom.Geom;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basisimplementierung für Figures, die mit den Standardfunktionen von JHotDraw
 * nicht auskommen.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public abstract class TCSFigure
    extends AbstractAttributedDecoratedFigure {

  /**
   * The enclosing rectangle.
   */
  protected Rectangle fDisplayBox;
  /**
   * Enthält die exakte Position des Mittelpunkts der Figur
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
   * @return den Zoompunkt
   */
  public ZoomPoint getZoomPoint() {
    return fZoomPoint;
  }

  /**
   * Setzt den ZoomPunkt.
   *
   * @param zoomPoint Der Zoompunkt *
   */
  public void setZoomPoint(ZoomPoint zoomPoint) {
    fZoomPoint = zoomPoint;
  }

  /**
   * Wird beim Erzeugen eines neuen Grafik-Objekts mit dem Creation Tool
   * aufgerufen. Dabei wird auch das zugehörige Modell ge-"cloned".
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

  /**
   * Returns the model object for this figure.
   *
   * @return The model object for this figure.
   */
  public ModelComponent getModel() {
    return get(FigureConstants.MODEL);
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
