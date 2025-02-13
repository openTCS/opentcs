// SPDX-FileCopyrightText: The original authors of JHotDraw and all its contributors
// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.thirdparty.operationsdesk.components.drawing;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
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
import java.util.Set;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.ApplicationState;
import org.opentcs.guing.common.components.drawing.figures.OriginFigure;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;
import org.opentcs.thirdparty.guing.common.jhotdraw.components.drawing.AbstractOpenTCSDrawingView;

/**
 * A DrawingView implementation for the openTCS plant overview.
 */
public class OpenTCSDrawingViewOperating
    extends
      AbstractOpenTCSDrawingView {

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
  public void followVehicle(
      @Nonnull
      final VehicleModel model
  ) {
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

    if (getEditor() == null
        || getEditor().getTool() == null
        || getEditor().getActiveView() != this) {
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
        = new RadialGradientPaint(
            center, radius, dist, colors,
            MultipleGradientPaint.CycleMethod.NO_CYCLE
        );

    Graphics2D gVehicle = (Graphics2D) g2d.create();
    gVehicle.setPaint(paint);
    gVehicle.fillRect(0, 0, getWidth(), getHeight());
    gVehicle.dispose();

    // After drawing the RadialGradientPaint the drawing area needs to
    // repainted, otherwise the GradientPaint isn't drawn correctly or
    // the old one isn't removed. We make sure the repaint() call doesn't
    // end in an infinite loop.
    loopProofRepaintDrawingArea();
  }

  private class ExtendedEventHandler
      extends
        AbstractExtendedEventHandler {

    /**
     * Creates a new instance.
     */
    ExtendedEventHandler() {
    }

    @Override
    protected boolean shouldShowFigure(Figure figure) {
      return !(figure instanceof VehicleFigure) && !(figure instanceof OriginFigure);
    }
  }
}
