/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.components.drawing.figures.FigureFactory;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.NamedVehicleFigure;
import org.opentcs.guing.components.drawing.figures.OffsetFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for Figures and ModelComponents.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CourseObjectFactory {

  /**
   * A factory for figures.
   */
  private final FigureFactory figureFactory;

  /**
   * Creates a new instance.
   *
   * @param figureFactory A factory for figures.
   */
  @Inject
  public CourseObjectFactory(FigureFactory figureFactory) {
    this.figureFactory = requireNonNull(figureFactory, "figureFactory");
  }

  public LayoutModel createLayoutModel() {
    return new LayoutModel();
  }

  public VehicleModel createVehicleModel() {
    return new VehicleModel();
  }

  public LocationTypeModel createLocationTypeModel() {
    return new LocationTypeModel();
  }

  public BlockModel createBlockModel() {
    return new BlockModel();
  }

  public GroupModel createGroupModel() {
    return new GroupModel();
  }

  public StaticRouteModel createStaticRouteModel() {
    return new StaticRouteModel();
  }

  public LabeledPointFigure createPointFigure() {
    PointFigure pointFigure = figureFactory.createPointFigure(new PointModel());
    return figureFactory.createLabeledPointFigure(pointFigure);
  }

  public PathConnection createPathConnection() {
    return figureFactory.createPathConnection(new PathModel());
  }

  public LabeledLocationFigure createLocationFigure() {
    LocationFigure location = figureFactory.createLocationFigure(new LocationModel());
    return figureFactory.createLabeledLocationFigure(location);
  }

  public LinkConnection createLinkConnection() {
    return figureFactory.createLinkConnection(new LinkModel());
  }

  public VehicleFigure createVehicleFigure(VehicleModel model) {
    return figureFactory.createVehicleFigure(model);
  }

  public NamedVehicleFigure createNamedVehicleFigure(VehicleModel model) {
    return figureFactory.createNamedVehicleFigure(model);
  }

  public OffsetFigure createOffsetFigure() {
    return figureFactory.createOffsetFigure();
  }
}
