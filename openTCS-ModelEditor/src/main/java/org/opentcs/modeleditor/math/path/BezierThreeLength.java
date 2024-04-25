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
 * Calculates the length of {@link PathModel.Type#BEZIER_3} paths.
 */
public class BezierThreeLength
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
  public BezierThreeLength(@Nonnull ModelManager manager,
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
        cps.length == 5,
        String.format("Path '%s' does not have exactly five control points.", path.getName())
    );

    PointModel start = (PointModel) path.getStartComponent();
    PointModel end = (PointModel) path.getEndComponent();

    return calculateBezierCurveLength(
        new Coordinate(start.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                       start.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM)),
        new Coordinate(Double.parseDouble(cps[0].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[0].split(",")[1]) * scaleY * (-1)),
        new Coordinate(Double.parseDouble(cps[1].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[1].split(",")[1]) * scaleY * (-1)),
        new Coordinate(Double.parseDouble(cps[2].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[2].split(",")[1]) * scaleY * (-1)),
        new Coordinate(Double.parseDouble(cps[3].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[3].split(",")[1]) * scaleY * (-1)),
        new Coordinate(Double.parseDouble(cps[4].split(",")[0]) * scaleX,
                       Double.parseDouble(cps[4].split(",")[1]) * scaleY * (-1)),
        new Coordinate(end.getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM),
                       end.getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM)));
  }

  private double calculateBezierCurveLength(Coordinate start, Coordinate cp0, Coordinate cp1,
                                            Coordinate cp2, Coordinate cp3, Coordinate cp4,
                                            Coordinate end) {
    return pathLengthMath.approximateCubicBezierCurveLength(start, cp0, cp1, cp2, 1000)
        + pathLengthMath.approximateCubicBezierCurveLength(cp2, cp3, cp4, end, 1000);
  }
}
