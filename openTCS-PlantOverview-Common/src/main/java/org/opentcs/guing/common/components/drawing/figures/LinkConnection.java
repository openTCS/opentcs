/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import com.google.inject.assistedinject.Assisted;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.handle.Handle;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PointModel;

/**
 * A dashed line that connects a decision point with a location.
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
   * Connects two figures.
   *
   * @param point The point figure to connect.
   * @param location The location figure to connect.
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

  @Override
  public boolean canConnect(Connector start) {
    return start.getOwner().get(FigureConstants.MODEL) instanceof LocationModel;
  }

  @Override // SimpleLineConnection
  public boolean canConnect(Connector start, Connector end) {
    ModelComponent modelStart = start.getOwner().get(FigureConstants.MODEL);
    ModelComponent modelEnd = end.getOwner().get(FigureConstants.MODEL);

    if (modelStart == null || modelEnd == null) {
      return false;
    }

    // Even though new links can now only be created starting from a location, we need this to
    // ensure backward campatibility for older models that may still have links with a point
    // as the start component. Otherwise those links would not be connected/drawn properly.
    if ((modelStart instanceof PointModel) && modelEnd instanceof LocationModel) {
      LocationModel location = (LocationModel) modelEnd;
      PointModel point = (PointModel) modelStart;

      return !location.hasConnectionTo(point);
    }

    if (modelStart instanceof LocationModel && (modelEnd instanceof PointModel)) {
      LocationModel location = (LocationModel) modelStart;
      PointModel point = (PointModel) modelEnd;

      return !point.hasConnectionTo(location);
    }

    return false;
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
    return getModel().getPropertyLayerWrapper().getValue().getLayer().getOrdinal();
  }

  @Override
  public boolean isVisible() {
    return super.isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayer().isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayerGroup().isVisible();
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
