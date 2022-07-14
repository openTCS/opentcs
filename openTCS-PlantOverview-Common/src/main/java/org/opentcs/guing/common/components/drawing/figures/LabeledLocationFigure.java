/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import org.jhotdraw.draw.handle.Handle;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.common.components.drawing.ZoomPoint;
import org.opentcs.guing.common.components.drawing.course.Origin;

/**
 * {@link LocationFigure} with a label.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LabeledLocationFigure
    extends LabeledFigure {

  /**
   * The tool tip text generator.
   */
  private final ToolTipTextGenerator textGenerator;

  /**
   * Creates a new instance.
   *
   * @param figure The presentation figure.
   * @param textGenerator The tool tip text generator.
   */
  @Inject
  public LabeledLocationFigure(@Assisted LocationFigure figure,
                               ToolTipTextGenerator textGenerator) {
    requireNonNull(figure, "figure");
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");

    setPresentationFigure(figure);
  }

  @Override
  public LocationFigure getPresentationFigure() {
    return (LocationFigure) super.getPresentationFigure();
  }

  @Override
  public Shape getShape() {
    return getPresentationFigure().getDrawingArea();
  }

  @Override
  public String getToolTipText(Point2D.Double p) {
    return textGenerator.getToolTipText(getPresentationFigure().getModel());
  }

  @Override
  public LabeledLocationFigure clone() {
    // Do NOT clone the label here.
    LabeledLocationFigure that = (LabeledLocationFigure) super.clone();

    if (that.getChildCount() > 0) {
      that.removeChild(0);
    }

    LocationFigure thatPresentationFigure = that.getPresentationFigure();
    thatPresentationFigure.addFigureListener(that.eventHandler);
    // Force loading of the symbol bitmap
    thatPresentationFigure.propertiesChanged(null);

    return that;
  }

  @Override
  public Collection<Action> getActions(Point2D.Double p) {
    LinkedList<Action> editOptions = new LinkedList<>();

    return editOptions;
  }

  @Override
  public Collection<Handle> createHandles(int detailLevel) {
    if (!isVisible()) {
      return new ArrayList<>();
    }

    return super.createHandles(detailLevel);
  }

  @Override
  public int getLayer() {
    return getPresentationFigure().getLayer();
  }

  @Override
  public boolean isVisible() {
    return getPresentationFigure().isVisible();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator().equals(this)) {
      return;
    }

    // Move the figure if the model coordinates have been changed in the
    // Properties panel
    Origin origin = get(FigureConstants.ORIGIN);

    if (origin != null) {
      LocationFigure lf = getPresentationFigure();

      if (lf.getModel().getPropertyLayoutPositionX().hasChanged()
          || lf.getModel().getPropertyLayoutPositionY().hasChanged()) {
        getLabel().willChange();
        Point2D exact
            = origin.calculatePixelPositionExactly(lf.getModel().getPropertyLayoutPositionX(),
                                                   lf.getModel().getPropertyLayoutPositionY());
        double scale = lf.getZoomPoint().scale();
        double xNew = exact.getX() / scale;
        double yNew = exact.getY() / scale;
        Point2D.Double anchor = new Point2D.Double(xNew, yNew);
        setBounds(anchor, anchor);
        getLabel().changed();
      }
    }

    // Update the image of the actual Location type
    getPresentationFigure().propertiesChanged(event);

    invalidate();
    // also update the label.
    fireFigureChanged();
  }

  @Override
  public void scaleModel(EventObject event) {
    Origin origin = get(FigureConstants.ORIGIN);

    if (origin != null) {
      LocationFigure lf = getPresentationFigure();

      Point2D exact
          = origin.calculatePixelPositionExactly(lf.getModel().getPropertyLayoutPositionX(),
                                                 lf.getModel().getPropertyLayoutPositionY());
      Point2D.Double anchor = new Point2D.Double(exact.getX(), exact.getY());
      setBounds(anchor, anchor);
    }

    invalidate();
    // also update the label.
    fireFigureChanged();
  }

  @Override
  public void updateModel() {
    Origin origin = get(FigureConstants.ORIGIN);
    LocationFigure lf = getPresentationFigure();
    LocationModel model = lf.getModel();
    CoordinateProperty cpx = model.getPropertyModelPositionX();
    CoordinateProperty cpy = model.getPropertyModelPositionY();
    if ((double) cpx.getValue() == 0.0 && (double) cpy.getValue() == 0.0) {
      origin.calculateRealPosition(lf.center(), cpx, cpy);
      cpx.markChanged();
      cpy.markChanged();
    }
    ZoomPoint zoomPoint = lf.getZoomPoint();
    if (zoomPoint != null && origin != null) {
      StringProperty lpx = model.getPropertyLayoutPositionX();
      int oldX = 0;

      if (!Strings.isNullOrEmpty(lpx.getText())) {
        oldX = (int) Double.parseDouble(lpx.getText());
      }
      int newX = (int) (zoomPoint.getX() * origin.getScaleX());

      if (newX != oldX) {
        lpx.setText(String.format("%d", newX));
        lpx.markChanged();
      }

      StringProperty lpy = model.getPropertyLayoutPositionY();

      int oldY = 0;
      if (!Strings.isNullOrEmpty(lpy.getText())) {
        oldY = (int) Double.parseDouble(lpy.getText());
      }

      int newY = (int) (-zoomPoint.getY() * origin.getScaleY());  // Vorzeichen!

      if (newY != oldY) {
        lpy.setText(String.format("%d", newY));
        lpy.markChanged();
      }

      // Offset of the labels of the location will be updated here.
      StringProperty propOffsetX = model.getPropertyLabelOffsetX();
      if (Strings.isNullOrEmpty(propOffsetX.getText())) {
        propOffsetX.setText(String.format("%d", TCSLabelFigure.DEFAULT_LABEL_OFFSET_X));
      }

      StringProperty propOffsetY = model.getPropertyLabelOffsetY();
      if (Strings.isNullOrEmpty(propOffsetY.getText())) {
        propOffsetY.setText(String.format("%d", TCSLabelFigure.DEFAULT_LABEL_OFFSET_Y));
      }

    }
    // update the type.
    model.getPropertyType().markChanged();

    model.propertiesChanged(this);
    // also update the label.
    fireFigureChanged();
  }
}
