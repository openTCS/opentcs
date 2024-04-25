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
import org.opentcs.guing.common.persistence.ModelManager;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Calculates the length of {@link PathModel.Type#POLYPATH} paths.
 */
public class PolyPathLength
    implements PathLengthFunction {

  private final double scaleX;
  private final double scaleY;
  private final PathLengthMath pathLengthMath;

  /**
   * Creates a new instance.
   *
   * @param manager Provides access to the current system model.
   * @param pathLengthMath Provides euclidean distance method.
   */
  @Inject
  public PolyPathLength(@Nonnull ModelManager manager,
                        @Nonnull PathLengthMath pathLengthMath) {
    requireNonNull(manager, "manager");
    this.pathLengthMath = requireNonNull(pathLengthMath, "pathLengthMath");
    scaleX = manager.getModel().getLayoutModel().getPropertyScaleX()
        .getValueByUnit(LengthProperty.Unit.MM);
    scaleY = manager.getModel().getLayoutModel().getPropertyScaleY()
        .getValueByUnit(LengthProperty.Unit.MM);
  }

  @Override
  public double applyAsDouble(PathModel path) {
    requireNonNull(path, "path");

    String[] cps = path.getPropertyPathControlPoints().getText().split(";");
    checkArgument(
        cps.length >= 1,
        String.format("Path '%s' does not have at least one control point.", path.getName())
    );

    Coordinate[] controlCoordinates = new Coordinate[cps.length + 2];
    controlCoordinates[0] = new Coordinate(
        ((PointModel) path.getStartComponent()).getPropertyModelPositionX()
            .getValueByUnit(LengthProperty.Unit.MM),
        ((PointModel) path.getStartComponent()).getPropertyModelPositionY()
            .getValueByUnit(LengthProperty.Unit.MM));

    for (int i = 0; i < cps.length; i++) {
      String couple = cps[i];
      double x = Double.parseDouble(couple.split(",")[0]) * scaleX;
      double y = Double.parseDouble(couple.split(",")[1]) * scaleY * (-1);
      controlCoordinates[i + 1] = new Coordinate(x, y);
    }

    controlCoordinates[cps.length + 1] = new Coordinate(
        ((PointModel) path.getEndComponent()).getPropertyModelPositionX()
            .getValueByUnit(LengthProperty.Unit.MM),
        ((PointModel) path.getEndComponent()).getPropertyModelPositionY()
            .getValueByUnit(LengthProperty.Unit.MM)
    );

    return calculatePolyPathDistance(controlCoordinates);
  }

  private double calculatePolyPathDistance(Coordinate[] controlCoordinates) {
    double length = 0;

    for (int i = 0; i < controlCoordinates.length - 1; i++) {
      length += pathLengthMath.euclideanDistance(controlCoordinates[i], controlCoordinates[i + 1]);
    }

    return length;
  }
}
