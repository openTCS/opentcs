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

import com.google.inject.assistedinject.Assisted;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.inject.Inject;
import org.jhotdraw.geom.Geom;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.model.elements.PointModel;

/**
 * Ein Figure-Objekt, das einen Meldepunkt darstellt. Das zugehörige Datenobjekt
 * ist vom Typ Point. Darstellung als Kreis mit 20 Layout-Units Durchmesser
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PointFigure
    extends TCSFigure {

  /**
   * A color for parking positions.
   */
  private static final Color C_PARK = Color.BLUE;
  /**
   * A color for report positions.
   */
  private static final Color C_REPORT = Color.WHITE;
  /**
   * A color for halt positions.
   */
  private static final Color C_HALT = Color.LIGHT_GRAY;
  /**
   * The figure's diameter in drawing units (pixels at 100% zoom).
   */
  private final int fDiameter;

  /**
   * Creates a new instance.
   *
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected
   * model component(s).
   * @param model The model corresponding to this graphical object.
   */
  @Inject
  public PointFigure(ComponentsTreeViewManager componentsTreeManager,
                     SelectionPropertiesComponent propertiesComponent,
                     @Assisted PointModel model) {
    super(componentsTreeManager, propertiesComponent, model);

    // TO DO: Grid Constrainer anpassen, sodass auch kleinere Figur auf das "10er" Raster gezogen wird.
    fDiameter = 10;
    fDisplayBox = new Rectangle(fDiameter, fDiameter);
    fZoomPoint = new ZoomPoint(0.5 * fDiameter, 0.5 * fDiameter);
  }

  @Override
  public PointModel getModel() {
    return (PointModel) get(FigureConstants.MODEL);
  }

  public Point center() {
    return Geom.center(fDisplayBox);
  }

  public Ellipse2D.Double getShape() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Ellipse2D.Double shape = new Ellipse2D.Double(r2.getX(), r2.getY(), fDiameter - 1, fDiameter - 1);
    return shape;
  }

  @Override  // Figure
  public Rectangle2D.Double getBounds() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Rectangle2D.Double r2d = new Rectangle2D.Double();
    r2d.setRect(r2);

    return r2d;
  }

  @Override  // Figure
  public Object getTransformRestoreData() {
    // Never used?
    return fDisplayBox.clone();
  }

  @Override  // Figure
  public void restoreTransformTo(Object restoreData) {
    // Never used?
    Rectangle r = (Rectangle) restoreData;
    fDisplayBox.x = r.x;
    fDisplayBox.y = r.y;
    fDisplayBox.width = r.width;
    fDisplayBox.height = r.height;
    fZoomPoint.setX(r.x + 0.5 * r.width);
    fZoomPoint.setY(r.y + 0.5 * r.height);
  }

  @Override  // Figure
  public void transform(AffineTransform tx) {
    Point2D center = getZoomPoint().getPixelLocationExactly();
    Point2D lead = new Point2D.Double();  // not used
    setBounds(
        (Point2D.Double) tx.transform(center, center),
        (Point2D.Double) tx.transform(lead, lead));
  }

  @Override  // AbstractFigure
  public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
    fZoomPoint.setX(anchor.x);
    fZoomPoint.setY(anchor.y);
    fDisplayBox.x = (int) (anchor.x - 0.5 * fDiameter);
    fDisplayBox.y = (int) (anchor.y - 0.5 * fDiameter);
  }

  @Override  // AbstractAttributedFigure
  protected void drawFill(Graphics2D g) {
    Rectangle rect = fDisplayBox;

    if (getModel().getPropertyType().getValue() == PointModel.PointType.PARK) {
      g.setColor(C_PARK);
    }
    else if (getModel().getPropertyType().getValue() == PointModel.PointType.REPORT) {
      g.setColor(C_REPORT);
    }
    else {
      g.setColor(C_HALT);
    }

    if (rect.width > 0 && rect.height > 0) {
      g.fillOval(rect.x, rect.y, rect.width, rect.height);
    }

    if (getModel().getPropertyType().getValue() == PointModel.PointType.PARK) {
      g.setColor(Color.white);
      Font oldFont = g.getFont();
      Font newFont = new Font(Font.DIALOG, Font.BOLD, 7);
      g.setFont(newFont);
      g.drawString("P", rect.x + 3, rect.y + rect.height - 3);
      g.setFont(oldFont);
    }
  }

  @Override  // AbstractAttributedFigure
  protected void drawStroke(Graphics2D g) {
    Rectangle r = fDisplayBox;

    if (r.width > 0 && r.height > 0) {
      g.drawOval(r.x, r.y, r.width - 1, r.height - 1);
    }
  }

  @Override // AbstractAttributedDecoratedFigure
  public PointFigure clone() {
    PointFigure thatFigure = (PointFigure) super.clone();
    thatFigure.setZoomPoint(new ZoomPoint(fZoomPoint.getX(), fZoomPoint.getY()));

    return thatFigure;
  }

  @Override // AbstractFigure
  public int getLayer() {
    // TODO: Layer / z-Order sinnvoll verwalten!
    return -1; // stay below ConnectionFigures
  }
}
