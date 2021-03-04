/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface FigureFactory {

  PointFigure createPointFigure(PointModel model);

  LabeledPointFigure createLabeledPointFigure(PointFigure figure);

  LocationFigure createLocationFigure(LocationModel model);

  LabeledLocationFigure createLabeledLocationFigure(LocationFigure figure);

  PathConnection createPathConnection(PathModel model);

  LinkConnection createLinkConnection(LinkModel model);

  VehicleFigure createVehicleFigure(VehicleModel model);

  NamedVehicleFigure createNamedVehicleFigure(VehicleModel model);

  OffsetFigure createOffsetFigure();
}
