/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.util;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.components.drawing.figures.FigureFactory;
import org.opentcs.guing.common.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.common.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.common.components.drawing.figures.LinkConnection;
import org.opentcs.guing.common.components.drawing.figures.LocationFigure;
import org.opentcs.guing.common.components.drawing.figures.OffsetFigure;
import org.opentcs.guing.common.components.drawing.figures.PathConnection;
import org.opentcs.guing.common.components.drawing.figures.PointFigure;

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

  public OffsetFigure createOffsetFigure() {
    return figureFactory.createOffsetFigure();
  }
}
