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
 * Calculates the length of {@link PathModel.Type#BEZIER} paths.
 */
public class BezierLength
    implements PathLengthFunction {

  private final double scaleX;
  private final double scaleY;
  private final PathLengthMath pathLengthMath;

  /**
   * Creates a new instance.
   *
   * @param manager Provides access to the current system model.
   * @param pathLengthMath Provides methods to evaluate the position of a point of a Bezier curve.
   */
  @Inject
  public BezierLength(@Nonnull ModelManager manager,
                      @Nonnull PathLengthMath pathLengthMath) {
    requireNonNull(manager, "manager");
    this.pathLengthMath = requireNonNull(pathLengthMath, "pathLengthMath");
    scaleX = manager.getModel().getLayoutModel().getPropertyScaleX()
        .getValueByUnit(LengthProperty.Unit.MM);
    scaleY = manager.getModel().getLayoutModel().getPropertyScaleY()
        .getValueByUnit(LengthProperty.Unit.MM);
  }

  @Override
  public double applyAsDouble(@Nonnull PathModel path) {
    requireNonNull(path, "path");

    String[] cps = path.getPropertyPathControlPoints().getText().split(";");
    checkArgument(
        cps.length == 2,
        String.format("Path '%s' does not have exactly two control points.", path.getName())
    );

    PointModel start = (PointModel) path.getStartComponent();
    PointModel end = (PointModel) path.getEndComponent();

    return pathLengthMath.approximateCubicBezierCurveLength(
        new Coordinate(start.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                       start.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM)),
        new Coordinate(Double.parseDouble(cps[0].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[0].split(",")[1]) * scaleY * (-1)),
        new Coordinate(Double.parseDouble(cps[1].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[1].split(",")[1]) * scaleY * (-1)),
        new Coordinate(end.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                       end.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM)),
        1000
    );
  }
}
