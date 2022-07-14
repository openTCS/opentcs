/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.EventObject;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.LineConnectionFigure;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.AbstractConnection;
import org.opentcs.guing.common.components.drawing.course.OriginChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class SimpleLineConnection
    extends LineConnectionFigure
    implements ModelBasedFigure,
               AttributesChangeListener,
               OriginChangeListener {

  protected static final AttributeKey<Color> FILL_COLOR
      = new AttributeKey<>("FillColor", Color.class);
  protected static final AttributeKey<Color> STROKE_COLOR
      = new AttributeKey<>("StrokeColor", Color.class);
  protected static final ArrowTip ARROW_FORWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, true);
  protected static final ArrowTip ARROW_BACKWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, false);
  private static final Logger logger
      = LoggerFactory.getLogger(SimpleLineConnection.class);

  /**
   * Creates a new instance.
   *
   * @param model The model corresponding to this graphical object.
   */
  public SimpleLineConnection(AbstractConnection model) {
    set(FigureConstants.MODEL, model);
    initConnectionFigure();
  }

  /**
   * Initialise this figure.
   */
  protected final void initConnectionFigure() {
    updateDecorations();
  }

  @Override
  public AbstractConnection getModel() {
    return (AbstractConnection) get(FigureConstants.MODEL);
  }

  /**
   * Return the shape.
   *
   * @return the shape.
   */
  public Shape getShape() {
    return path;
  }

  @Override // BezierFigure
  protected BezierPath getCappedPath() {
    // Workaround for NullPointerException in BezierFigure.getCappedPath()
    try {
      return super.getCappedPath();
    }
    catch (NullPointerException ex) {
      logger.warn("", ex);
      return path.clone();
    }
  }

  /**
   * Update the properties of the model.
   */
  public abstract void updateModel();

  /**
   * Scales the model coodinates accodring to changes to the layout scale.
   *
   * @param event The event containing the layout scale change.
   */
  public abstract void scaleModel(EventObject event);

  /**
   * Calculates the euclid distance between the start position and the end position.
   *
   * @param startPosX The x coordiante of the start position.
   * @param startPosY The y coordiante of the start position.
   * @param endPosX The x coordinate of the end position.
   * @param endPosY The y coordinate of the end position.
   * @return the euclid distance between start and end point rounded to the next integer.
   */
  protected double distance(double startPosX, double startPosY, double endPosX, double endPosY) {
    double dX = startPosX - endPosX;
    double dY = startPosY - endPosY;
    double dist = Math.sqrt(dX * dX + dY * dY);
    dist = Math.floor(dist + 0.5);  // round to an integer value.

    return dist;
  }

  public void updateDecorations() {
  }

  @Override // LineConnectionFigure
  protected void handleConnect(Connector start, Connector end) {
    if (start != null && end != null) {
      ModelComponent startModel = start.getOwner().get(FigureConstants.MODEL);
      ModelComponent endModel = end.getOwner().get(FigureConstants.MODEL);
      getModel().setConnectedComponents(startModel, endModel);
      updateModel();
    }
  }

  @Override // LineConnectionFigure
  protected void handleDisconnect(Connector start, Connector end) {
    super.handleDisconnect(start, end);

    getModel().removingConnection();
  }

  @Override // LineConnectionFigure
  public boolean handleMouseClick(Point2D.Double p, MouseEvent evt, DrawingView drawingView) {
    return false;
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (!e.getInitiator().equals(this)) {
      updateDecorations();
      fireFigureChanged(getDrawingArea());
    }
  }

  @Override // OriginChangeListener
  public void originLocationChanged(EventObject event) {
  }

  @Override // OriginChangeListener
  public void originScaleChanged(EventObject event) {
    scaleModel(event);
  }

  @Override
  public SimpleLineConnection clone() {
    try {
      SimpleLineConnection clone = (SimpleLineConnection) super.clone();
      clone.set(FigureConstants.MODEL, getModel().clone());

      return clone;
    }
    catch (CloneNotSupportedException exc) {
      throw new IllegalStateException("Unexpected exception encountered", exc);
    }
  }
}
