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
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * Eine gestrichelte Linie, die einen Meldepunkt mit einer Übergabestation oder
 * einer Batterieladestation verbindet. Eine SimpleLineConnection besitzt keine
 * Pfeilspitzen.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LinkConnection
    extends SimpleLineConnection {

  /**
   * The tool tip text generator.
   */
  private final ToolTipTextGenerator textGenerator;

  /**
   * Creates a new instance.
   *
   * @param model The model corresponding to this graphical object.
   * @param textGenerator The tool tip text generator.
   */
  @Inject
  public LinkConnection(@Assisted LinkModel model,
                        ToolTipTextGenerator textGenerator) {
    super(model);
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");

    double[] dash = {5.0, 5.0};
    set(AttributeKeys.START_DECORATION, null);
    set(AttributeKeys.END_DECORATION, null);
    set(AttributeKeys.STROKE_WIDTH, 1.0);
    set(AttributeKeys.STROKE_CAP, BasicStroke.CAP_BUTT);
    set(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_MITER);
    set(AttributeKeys.STROKE_MITER_LIMIT, 1.0);
    set(AttributeKeys.STROKE_DASHES, dash);
    set(AttributeKeys.STROKE_DASH_PHASE, 0.0);
  }

  @Override
  public LinkModel getModel() {
    return (LinkModel) get(FigureConstants.MODEL);
  }

  /**
   * Verknüpft zwei Figure-Objekte durch diese Verbindung.
   *
   * @param point das erste Figure-Objekt
   * @param location das zweite Figure-Objekt
   */
  public void connect(LabeledPointFigure point, LabeledLocationFigure location) {
    Connector compConnector = new ChopEllipseConnector();
    Connector startConnector = point.findCompatibleConnector(compConnector, true);
    Connector endConnector = location.findCompatibleConnector(compConnector, true);

    if (!canConnect(startConnector, endConnector)) {
      return;
    }

    setStartConnector(startConnector);
    setEndConnector(endConnector);
  }

  @Override // AbstractFigure
  public String getToolTipText(Point2D.Double p) {
    return textGenerator.getToolTipText(getModel());
  }

  @Override // SimpleLineConnection
  public boolean canConnect(Connector start, Connector end) {
    ModelComponent modelStart = start.getOwner().get(FigureConstants.MODEL);
    ModelComponent modelEnd = end.getOwner().get(FigureConstants.MODEL);

    if (modelStart == null || modelEnd == null) {
      return false;
    }

    if ((modelStart instanceof PointModel) && modelEnd instanceof LocationModel) {
      LocationModel location = (LocationModel) modelEnd;
      PointModel point = (PointModel) modelStart;

      return !location.hasConnectionTo(point);
    }

    if (modelStart instanceof LocationModel && (modelEnd instanceof PointModel)) {
      LocationModel location = (LocationModel) modelStart;
      PointModel point = (PointModel) modelEnd;

      return !location.hasConnectionTo(point);
    }

    return false;
  }

  @Override
  public void updateModel() {
  }

  @Override
  public void scaleModel(EventObject event) {
  }

  @Override
  public LinkConnection clone() {
    LinkConnection clone = (LinkConnection) super.clone();
    clone.initConnectionFigure();

    return clone;
  }
}
