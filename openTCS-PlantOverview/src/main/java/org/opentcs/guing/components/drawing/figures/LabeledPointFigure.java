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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
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
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.decoration.PointOutlineHandle;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.elements.PointModel;

/**
 * LabeledPointFigure: PointFigure mit zugehörigem Label, das mit der Figur
 * bewegt wird.
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
//    double min = java.lang.Double.MAX_VALUE;
//    Connector closest = null;
//
//    for (Connector c : connectors) {
//      Point2D.Double p2 = Geom.center(c.getBounds());
//      double d2 = Geom.length2(p.x, p.y, p2.x, p2.y);  // distance^2
//
//      if (d2 < min) {
//        min = d2;
//        closest = c;
//
////        if (min == 0.0) {
////          break;
////        }
//      }
//    }
//
//    return closest;
    return (new ChopEllipseConnector(this));
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
  public void read(DOMInput in)
      throws IOException {
    double x = in.getAttribute("x", 0d);
    double y = in.getAttribute("y", 0d);
    setBounds(new Point2D.Double(x, y), new Point2D.Double(x, y));
  }

  @Override
  public void write(DOMOutput out)
      throws IOException {
    PointFigure pf = getPresentationFigure();
    out.addAttribute("x", pf.getZoomPoint().getX());
    out.addAttribute("y", pf.getZoomPoint().getY());
    out.addAttribute("name", get(FigureConstants.MODEL).getName());
  }

  @Override
  public Collection<Action> getActions(Point2D.Double p) {
    LinkedList<Action> editOptions = new LinkedList<>();
//    editOptions.add(new CutAction());
//    editOptions.add(new CopyAction());
//    editOptions.add(new PasteAction());
//    editOptions.add(new DuplicateAction());

    return editOptions;
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
    // Auch das Label aktualisieren
    fireFigureChanged();
  }

  @Override
  public void scaleModel(EventObject event) {
    Origin origin = get(FigureConstants.ORIGIN);

    if (origin != null) {
      PointFigure pf = getPresentationFigure();

//      getLabel().willChange();
      Point2D exact = origin.calculatePixelPositionExactly(pf.getModel().getPropertyLayoutPosX(),
                                                           pf.getModel().getPropertyLayoutPosY());
      Point2D.Double anchor = new Point2D.Double(exact.getX(), exact.getY());
      setBounds(anchor, anchor);
//      getLabel().changed();
    }

    invalidate();
    // Auch das Label aktualisieren
    fireFigureChanged();
  }

  @Override
  public void updateModel() {
    Origin origin = get(FigureConstants.ORIGIN);
    PointFigure pf = getPresentationFigure();
    PointModel model = pf.getModel();
    CoordinateProperty cpx = model.getPropertyModelPositionX();
    CoordinateProperty cpy = model.getPropertyModelPositionY();
    // Schreibt die aktuellen Modell-Koordinaten in die Properties
    if ((double) cpx.getValue() == 0.0 && (double) cpy.getValue() == 0.0) {
      // Koordinaten nur einmal beim Erzeugen aus Layout übernehmen
      origin.calculateRealPosition(pf.center(), cpx, cpy);
      cpx.markChanged();
      cpy.markChanged();
    }
    // Schreibt die aktuellen Layout-Koordinaten in die Properties
    ZoomPoint zoomPoint = pf.getZoomPoint();
    // Wenn die Figure gerade gelöscht wurde, kann der Origin schon null sein
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
    }
    // Immer den Typ aktualisieren
    model.getPropertyType().markChanged();

    model.propertiesChanged(this);
    // Auch das Label aktualisieren
    fireFigureChanged();
  }

  @Override
  public Collection<Handle> createHandles(int detailLevel) {
    LinkedList<Handle> handles = new LinkedList<>();

    switch (detailLevel) {
      case -1: // Mouse Moved
        handles.add(new PointOutlineHandle(getPresentationFigure()));
        break;

      case 0:  // Mouse clicked
        // 4 Rechteckige Move Handles in den Ecken der Figur
        MoveHandle.addMoveHandles(this, handles);
        // 4 Rechteckige Move Handles in den Ecken des Labels
        for (Figure child : getChildren()) {
          MoveHandle.addMoveHandles(child, handles);
          handles.add(new DragHandle(child));
        }

        break;

      case 1:  // Double-Click
        // Blauer Rahemen + 8 kleine blaue Resize Handles an den Ecken und den Seiten der Figur
        // TODO: Figur "springt" in die falsche Richtung!
        ResizeHandleKit.addResizeHandles(this, handles);
        break;

      default:
        break;
    }

    return handles;
  }
}
