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
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.Action;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.handle.DragHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.handle.MoveHandle;
import org.jhotdraw.draw.handle.ResizeHandleKit;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.type.CoordinateProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.components.drawing.ZoomPoint;
import org.opentcs.guing.common.components.drawing.course.Origin;
import org.opentcs.guing.common.components.drawing.figures.decoration.PointOutlineHandle;

/**
 * {@link PointFigure} with a label.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LabeledPointFigure
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
  public LabeledPointFigure(@Assisted PointFigure figure,
                            ToolTipTextGenerator textGenerator) {
    requireNonNull(figure, "figure");
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");

    setPresentationFigure(figure);
  }

  @Override
  public PointFigure getPresentationFigure() {
    return (PointFigure) super.getPresentationFigure();
  }

  @Override
  public Shape getShape() {
    return getPresentationFigure().getShape();
  }

  @Override
  public Connector findConnector(Point2D.Double p, ConnectionFigure prototype) {
    return new ChopEllipseConnector(this);
  }

  @Override
  public String getToolTipText(Point2D.Double p) {
    return textGenerator.getToolTipText(getPresentationFigure().getModel());
  }

  @Override
  public LabeledPointFigure clone() {
    // Do NOT clone the label here.
    LabeledPointFigure that = (LabeledPointFigure) super.clone();

    if (that.getChildCount() > 0) {
      that.basicRemoveAllChildren();
    }

    return that;
  }

  @Override
  public Collection<Action> getActions(Point2D.Double p) {
    LinkedList<Action> editOptions = new LinkedList<>();
    return editOptions;
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
      PointFigure pf = getPresentationFigure();

      StringProperty xLayout = pf.getModel().getPropertyLayoutPosX();
      StringProperty yLayout = pf.getModel().getPropertyLayoutPosY();

      if (xLayout.hasChanged() || yLayout.hasChanged()) {
        getLabel().willChange();
        Point2D exact = origin.calculatePixelPositionExactly(xLayout, yLayout);
        double scale = pf.getZoomPoint().scale();
        double xNew = exact.getX() / scale;
        double yNew = exact.getY() / scale;
        Point2D.Double anchor = new Point2D.Double(xNew, yNew);
        setBounds(anchor, anchor);
        getLabel().changed();
      }
    }

    invalidate();
    fireFigureChanged();
  }

  @Override
  public void scaleModel(EventObject event) {
    Origin origin = get(FigureConstants.ORIGIN);

    if (origin != null) {
      PointFigure pf = getPresentationFigure();

      Point2D exact = origin.calculatePixelPositionExactly(pf.getModel().getPropertyLayoutPosX(),
                                                           pf.getModel().getPropertyLayoutPosY());
      Point2D.Double anchor = new Point2D.Double(exact.getX(), exact.getY());
      setBounds(anchor, anchor);
    }

    invalidate();
    fireFigureChanged();
  }

  @Override
  public void updateModel() {
    Origin origin = get(FigureConstants.ORIGIN);
    PointFigure pf = getPresentationFigure();
    PointModel model = pf.getModel();
    CoordinateProperty cpx = model.getPropertyModelPositionX();
    CoordinateProperty cpy = model.getPropertyModelPositionY();
    // Write current model position to properties once when creating the layout.
    if ((double) cpx.getValue() == 0.0 && (double) cpy.getValue() == 0.0) {
      origin.calculateRealPosition(pf.center(), cpx, cpy);
      cpx.markChanged();
      cpy.markChanged();
    }
    ZoomPoint zoomPoint = pf.getZoomPoint();
    if (zoomPoint != null && origin != null) {
      StringProperty lpx = model.getPropertyLayoutPosX();

      int oldX = 0;
      if (!Strings.isNullOrEmpty(lpx.getText())) {
        oldX = (int) Double.parseDouble(lpx.getText());
      }

      int newX = (int) (zoomPoint.getX() * origin.getScaleX());
      if (newX != oldX) {
        lpx.setText(String.format("%d", newX));
        lpx.markChanged();
      }

      StringProperty lpy = model.getPropertyLayoutPosY();

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
      StringProperty propOffsetX = model.getPropertyPointLabelOffsetX();
      if (Strings.isNullOrEmpty(propOffsetX.getText())) {
        propOffsetX.setText(String.format("%d", TCSLabelFigure.DEFAULT_LABEL_OFFSET_X));
      }

      StringProperty propOffsetY = model.getPropertyPointLabelOffsetY();
      if (Strings.isNullOrEmpty(propOffsetY.getText())) {
        propOffsetY.setText(String.format("%d", TCSLabelFigure.DEFAULT_LABEL_OFFSET_Y));
      }

    }
    model.getPropertyType().markChanged();

    model.propertiesChanged(this);
    fireFigureChanged();
  }

  @Override
  public Collection<Handle> createHandles(int detailLevel) {
    LinkedList<Handle> handles = new LinkedList<>();

    if (!isVisible()) {
      return handles;
    }

    switch (detailLevel) {
      case -1: // Mouse Moved
        handles.add(new PointOutlineHandle(getPresentationFigure()));
        break;

      case 0:  // Mouse clicked
        MoveHandle.addMoveHandles(this, handles);
        for (Figure child : getChildren()) {
          MoveHandle.addMoveHandles(child, handles);
          handles.add(new DragHandle(child));
        }

        break;

      case 1:  // Double-Click
        ResizeHandleKit.addResizeHandles(this, handles);
        break;

      default:
        break;
    }

    return handles;
  }
}
