// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.drawing.figures;

import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;

/**
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
