/**
 * (c): IML, JHotDraw.
 *
 *
 * Extended by IML: 1. Show Blocks and Pathes as overlay 2. Switch labels on/off
 *
 * @(#)DefaultDrawingView.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.opentcs.thirdparty.operationsdesk.components.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.components.drawing.figures.OriginFigure;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;
import org.opentcs.thirdparty.guing.common.jhotdraw.components.drawing.AbstractOpenTCSDrawingView;

/**
 * A DrawingView implementation for the openTCS plant overview.
 *
 */
public class OpenTCSDrawingViewOperating
    extends AbstractOpenTCSDrawingView {

  /**
   * Contains the vehicle on the drawing, for which transport order shall be drawn.
   */
  private final List<VehicleModel> fVehicles = new ArrayList<>();
  /**
   * The vehicle the view should highlight and follow.
   */
  private VehicleModel fFocusVehicle;

  /**
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public OpenTCSDrawingViewOperating(ApplicationState appState, ModelManager modelManager) {
    super(appState, modelManager);
  }

  @Override
  public void removeAll() {
    fVehicles.clear();
    super.removeAll();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);

    if (evt.getPropertyName().equals(VehicleFigure.POSITION_CHANGED)) {
      scrollTo((VehicleFigure) getModelManager().getModel().getFigure(fFocusVehicle));
    }
  }

  @Override
  public void cutSelectedItems() {
  }

  @Override
  public void copySelectedItems() {
  }

  @Override
  public void pasteBufferedItems() {
  }

  @Override
  public void delete() {
  }

  @Override
  public void duplicate() {
  }

  @Override
  public void displayDriveOrders(VehicleModel vehicle, boolean visible) {
    requireNonNull(vehicle, "vehicle");

    if (visible) {
      if (!fVehicles.contains(vehicle)) {
        fVehicles.add(vehicle);
      }
    }
    else {
      fVehicles.remove(vehicle);
    }
  }

  @Override
  public void followVehicle(@Nonnull final VehicleModel model) {
    requireNonNull(model, "model");

    stopFollowVehicle();
    fFocusVehicle = model;
    fFocusVehicle.setViewFollows(true);
    VehicleFigure vFigure = (VehicleFigure) getModelManager().getModel().getFigure(fFocusVehicle);
    if (vFigure != null) {
      vFigure.addPropertyChangeListener(this);
      scrollTo(vFigure);
    }
  }

  @Override
  public void stopFollowVehicle() {
    if (fFocusVehicle == null) {
      return;
    }

    fFocusVehicle.setViewFollows(false);
    VehicleFigure vFigure = (VehicleFigure) getModelManager().getModel().getFigure(fFocusVehicle);
    if (vFigure != null) {
      vFigure.removePropertyChangeListener(this);
    }
    fFocusVehicle = null;
    repaint();
  }

  @Override
  public void setBlocks(ModelComponent blocks) {
  }
 
  @Override
  protected void drawTool(Graphics2D g2d) {
    super.drawTool(g2d);

    if (getEditor() == null || getEditor().getTool() == null || getEditor().getActiveView() != this) {
      return;
    }

    if (fFocusVehicle != null) {
      // Set focus on the selected vehicle and its destination point
      highlightVehicle(g2d);
    }
    else {
      // Set focus on the selected figure
      highlightFocus(g2d);
    }
  }

  @Override
  protected DefaultDrawingView.EventHandler createEventHandler() {
    return new ExtendedEventHandler();
  }

  @Override
  protected Rectangle2D.Double computeBounds(Figure figure) {
    Rectangle2D.Double bounds = super.computeBounds(figure);

    if (figure instanceof VehicleFigure) {
      // Also show the target point
      VehicleModel vehicleModel = ((VehicleFigure) figure).getModel();
      PointModel pointModel = vehicleModel.getNextPoint();

      if (pointModel != null) {
        Figure pointFigure = getModelManager().getModel().getFigure(pointModel);
        bounds.add(pointFigure.getBounds());
      }
    }

    return bounds;
  }

  @Override
  public void delete(Set<ModelComponent> components) {
  }

  /**
   * Sets a radial gradient for the vehicle, its current and next position.
   *
   * @param g2d
   */
  private void highlightVehicle(Graphics2D g2d) {
    if (fFocusVehicle == null) {
      return;
    }

    final Figure currentVehicleFigure = getModelManager().getModel().getFigure(fFocusVehicle);
    if (currentVehicleFigure == null) {
      return;
    }

    Rectangle2D.Double bounds = currentVehicleFigure.getBounds();
    double xCenter = bounds.getCenterX();
    double yCenter = bounds.getCenterY();
    Point2D.Double pCenterView = new Point2D.Double(xCenter, yCenter);
    Point pCenterDrawing = drawingToView(pCenterView);

    // radial gradient for the vehicle
    Point2D center
        = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);
    float radius = 30;
    float[] dist = {0.0f, 0.7f, 0.8f, 1.0f};
    Color[] colors = {
      new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
      new Color(1.0f, 1.0f, 1.0f, 0.0f),
      new Color(1.0f, 0.0f, 0.0f, 0.7f), // Circle: red
      new Color(0f, 0f, 0f, 0f) // Background
    };
    RadialGradientPaint paint
        = new RadialGradientPaint(center, radius, dist, colors,
                                  MultipleGradientPaint.CycleMethod.NO_CYCLE);

    Graphics2D gVehicle = (Graphics2D) g2d.create();
    gVehicle.setPaint(paint);
    gVehicle.fillRect(0, 0, getWidth(), getHeight());
    gVehicle.dispose();

    // radial gradient for the next position
    PointModel pointModel = fFocusVehicle.getNextPoint();

    if (pointModel != null) {
      Figure nextPoint = getModelManager().getModel().getFigure(pointModel);
      bounds = nextPoint.getBounds();
      xCenter = bounds.getCenterX();
      yCenter = bounds.getCenterY();
      pCenterView = new Point2D.Double(xCenter, yCenter);
      pCenterDrawing = drawingToView(pCenterView);
      center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);

      radius = 20;
      Color[] colorsGreen = {
        new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
        new Color(1.0f, 1.0f, 1.0f, 0.0f),
        new Color(0.0f, 1.0f, 0.0f, 0.7f), // Circle: green
        new Color(0f, 0f, 0f, 0f) // Background
      };
      paint = new RadialGradientPaint(center, radius, dist, colorsGreen,
                                      MultipleGradientPaint.CycleMethod.NO_CYCLE);

      Graphics2D gNextPosition = (Graphics2D) g2d.create();
      gNextPosition.setPaint(paint);
      gNextPosition.fillRect(0, 0, getWidth(), getHeight());
      gNextPosition.dispose();
    }

    // radial gradient for last position
    pointModel = fFocusVehicle.getPoint();

    if (pointModel != null && fFocusVehicle.getPrecisePosition() != null) {
      Figure lastPoint = getModelManager().getModel().getFigure(pointModel);
      bounds = lastPoint.getBounds();
      xCenter = bounds.getCenterX();
      yCenter = bounds.getCenterY();
      pCenterView = new Point2D.Double(xCenter, yCenter);
      pCenterDrawing = drawingToView(pCenterView);
      center = new Point2D.Float((float) pCenterDrawing.x, (float) pCenterDrawing.y);

      radius = 20;
      Color[] colorsBlue = {
        new Color(1.0f, 1.0f, 1.0f, 0.0f), // Focus: 100% transparent
        new Color(1.0f, 1.0f, 1.0f, 0.0f),
        new Color(0.0f, 0.0f, 1.0f, 0.7f), // Circle: blue
        new Color(0f, 0f, 0f, 0f) // Background
      };
      paint = new RadialGradientPaint(center, radius, dist, colorsBlue,
                                      MultipleGradientPaint.CycleMethod.NO_CYCLE);

      Graphics2D gCurrentPosition = (Graphics2D) g2d.create();
      gCurrentPosition.setPaint(paint);
      gCurrentPosition.fillRect(0, 0, getWidth(), getHeight());
      gCurrentPosition.dispose();
    }

    // After drawing the RadialGradientPaint the drawing area needs to
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    loopProofRepaintDrawingArea();
  }

  private class ExtendedEventHandler
      extends AbstractExtendedEventHandler {

    @Override
    protected boolean shouldShowFigure(Figure figure) {
      return !(figure instanceof VehicleFigure) && !(figure instanceof OriginFigure);
    }
  }
}
