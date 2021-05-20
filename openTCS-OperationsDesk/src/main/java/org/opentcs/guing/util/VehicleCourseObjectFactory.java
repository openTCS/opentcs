/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.guing.components.drawing.figures.NamedVehicleFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigureFactory;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 * A factory for Figures and ModelComponents.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleCourseObjectFactory
    extends CourseObjectFactory  {

  /**
   * A factory for figures.
   */
  private final VehicleFigureFactory figureFactory;

  /**
   * Creates a new instance.
   *
   * @param figureFactory A factory for figures.
   */
  @Inject
  public VehicleCourseObjectFactory(VehicleFigureFactory figureFactory) {
    super(figureFactory);
    this.figureFactory = requireNonNull(figureFactory, "figureFactory");
  }

  public VehicleFigure createVehicleFigure(VehicleModel model) {
    return figureFactory.createVehicleFigure(model);
  }

  public NamedVehicleFigure createNamedVehicleFigure(VehicleModel model) {
    return figureFactory.createNamedVehicleFigure(model);
  }
}
