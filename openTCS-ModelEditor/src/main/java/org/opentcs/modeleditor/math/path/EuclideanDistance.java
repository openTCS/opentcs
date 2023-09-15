/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;

/**
 * Calculates the length of a path as the Euclidean distance between a the start and end point.
 */
public class EuclideanDistance
    implements PathLengthFunction {

  /**
   * Creates a new instance.
   */
  @Inject
  public EuclideanDistance() {
  }

  @Override
  public double applyAsDouble(@Nonnull PathModel path) {
    requireNonNull(path, "path");

    PointModel start = (PointModel) path.getStartComponent();
    PointModel end = (PointModel) path.getEndComponent();

    double xDif = start.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM)
        - end.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM);
    double yDif = start.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM)
        - end.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM);
    return Math.sqrt(xDif * xDif + yDif * yDif);
  }
}
