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
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.elements.LocationModel;

/**
 * LabeledLocationFigure: LocationFigure mit zugehörigem Label, das mit der
 * Figur bewegt wird.
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
  public void read(DOMInput in)
      throws IOException {
    double x = in.getAttribute("x", 0d);
    double y = in.getAttribute("y", 0d);
    setBounds(new Point2D.Double(x, y), new Point2D.Double(x, y));
  }

  @Override
  public void write(DOMOutput out)
      throws IOException {
    LocationFigure lf = getPresentationFigure();
    out.addAttribute("x", lf.getZoomPoint().getX());
    out.addAttribute("y", lf.getZoomPoint().getY());
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
    // Auch das Label aktualisieren
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
    // Auch das Label aktualisieren
    fireFigureChanged();
  }

  @Override
  public void updateModel() {
    Origin origin = get(FigureConstants.ORIGIN);
    LocationFigure lf = getPresentationFigure();
    LocationModel model = lf.getModel();
    CoordinateProperty cpx = model.getPropertyModelPositionX();
    CoordinateProperty cpy = model.getPropertyModelPositionY();
    // Schreibt die aktuellen Modell-Koordinaten in die Properties
    if ((double) cpx.getValue() == 0.0 && (double) cpy.getValue() == 0.0) {
      // Koordinaten nur einmal beim Erzeugen aus Layout übernehmen
      origin.calculateRealPosition(lf.center(), cpx, cpy);
      cpx.markChanged();
      cpy.markChanged();
    }
    // Schreibt die aktuellen Layout-Koordinaten in die Properties
    ZoomPoint zoomPoint = lf.getZoomPoint();
    // Wenn die Figure gerade gelöscht wurde, kann der Origin schon null sein
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
    }
    // Immer den Typ aktualisieren
    model.getPropertyType().markChanged();

    model.propertiesChanged(this);
    // Auch das Label aktualisieren
    fireFigureChanged();
  }
}
