/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;

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

  OffsetFigure createOffsetFigure();
}
