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
import static org.mockito.Mockito.verify;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Test for {@link BezierLength}.
 */
class BezierLengthTest {

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
    pointModelStart.getPropertyModelPositionX().setValueAndUnit(10, LengthProperty.Unit.MM);
    pointModelStart.getPropertyModelPositionY().setValueAndUnit(10, LengthProperty.Unit.MM);

    PointModel pointModelEnd = new PointModel();
    pointModelEnd.getPropertyModelPositionX().setValueAndUnit(60, LengthProperty.Unit.MM);
    pointModelEnd.getPropertyModelPositionY().setValueAndUnit(60, LengthProperty.Unit.MM);

    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathControlPoints().setText("0.4,-0.6;0.8,-1");
    pathModel.setConnectedComponents(pointModelStart, pointModelEnd);

    LayoutModel layoutModel = new LayoutModel();
    layoutModel.getPropertyScaleX().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    layoutModel.getPropertyScaleY().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    given(systemModel.getLayoutModel()).willReturn(layoutModel);

    given(pathLengthMath.approximateCubicBezierCurveLength(any(), any(), any(), any(), anyDouble()))
        .willReturn(47.11);

    BezierLength function = new BezierLength(manager, pathLengthMath);
    double calculatedLength = function.applyAsDouble(pathModel);

    assertThat(calculatedLength).isEqualTo(47.11);
    verify(pathLengthMath).approximateCubicBezierCurveLength(new Coordinate(10, 10),
                                                             new Coordinate(20, 30),
                                                             new Coordinate(40, 50),
                                                             new Coordinate(60, 60),
                                                             1000);
  }
}
