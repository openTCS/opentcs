/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Test for {@link BezierThreeLength}.
 */
class BezierThreeLengthTest {

  private ModelManager manager;
  private SystemModel systemModel;
  private PathLengthMath pathLengthMath;

  @BeforeEach
  void setUp() {
    manager = mock();
    systemModel = mock();
    pathLengthMath = mock();
    given(manager.getModel()).willReturn(systemModel);
  }

  @Test
  void testApplyAsDouble() {
    PointModel pointModelStart = new PointModel();
    pointModelStart.getPropertyModelPositionX().setValueAndUnit(0, LengthProperty.Unit.MM);
    pointModelStart.getPropertyModelPositionY().setValueAndUnit(0, LengthProperty.Unit.MM);

    PointModel pointModelEnd = new PointModel();
    pointModelEnd.getPropertyModelPositionX().setValueAndUnit(10000, LengthProperty.Unit.MM);
    pointModelEnd.getPropertyModelPositionY().setValueAndUnit(10000, LengthProperty.Unit.MM);

    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathControlPoints().setText("36,-35;68,-67;100,-99;132,-131;164,-163");
    pathModel.setConnectedComponents(pointModelStart, pointModelEnd);

    LayoutModel layoutModel = new LayoutModel();
    layoutModel.getPropertyScaleX().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    layoutModel.getPropertyScaleY().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    given(systemModel.getLayoutModel()).willReturn(layoutModel);

    given(pathLengthMath.approximateCubicBezierCurveLength(any(), any(), any(), any(), anyDouble()))
        .willReturn(47.11);

    BezierThreeLength function = new BezierThreeLength(manager, pathLengthMath);
    double calculatedLength = function.applyAsDouble(pathModel);

    assertThat(calculatedLength).isEqualTo(94.22);
    verify(pathLengthMath, times(2))
        .approximateCubicBezierCurveLength(any(), any(), any(), any(), anyDouble());
    verify(pathLengthMath).approximateCubicBezierCurveLength(new Coordinate(0, 0),
                                                             new Coordinate(1800, 1750),
                                                             new Coordinate(3400, 3350),
                                                             new Coordinate(5000, 4950),
                                                             1000);
    verify(pathLengthMath).approximateCubicBezierCurveLength(new Coordinate(5000, 4950),
                                                             new Coordinate(6600, 6550),
                                                             new Coordinate(8200, 8150),
                                                             new Coordinate(10000, 10000),
                                                             1000);
  }
}
