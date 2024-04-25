/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.model.SystemModel;

/**
 * Test for {@link PolyPathLength}.
 */
class PolyPathLengthTest {

  private ModelManager manager;
  private SystemModel systemModel;

  @BeforeEach
  void setUp() {
    manager = mock();
    systemModel = mock();
    given(manager.getModel()).willReturn(systemModel);

    LayoutModel layoutModel = new LayoutModel();
    layoutModel.getPropertyScaleX().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    layoutModel.getPropertyScaleY().setValueAndUnit(50.0, LengthProperty.Unit.MM);
    given(systemModel.getLayoutModel()).willReturn(layoutModel);
  }

  @Test
  void testApplyAsDoubleOneControlPoint() {
    PointModel pointModelStart = new PointModel();
    pointModelStart.getPropertyModelPositionX().setValueAndUnit(10, LengthProperty.Unit.MM);
    pointModelStart.getPropertyModelPositionY().setValueAndUnit(10, LengthProperty.Unit.MM);

    PointModel pointModelEnd = new PointModel();
    pointModelEnd.getPropertyModelPositionX().setValueAndUnit(30, LengthProperty.Unit.MM);
    pointModelEnd.getPropertyModelPositionY().setValueAndUnit(20, LengthProperty.Unit.MM);

    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathControlPoints().setText("0.2,-0.4");
    pathModel.setConnectedComponents(pointModelStart, pointModelEnd);

    PolyPathLength function = new PolyPathLength(manager, new PathLengthMath());
    double calculatedLength = function.applyAsDouble(pathModel);

    assertThat(calculatedLength, is(30.0));
  }

  @Test
  void testApplyAsDoubleTwoControlPoints() {
    PointModel pointModelStart = new PointModel();
    pointModelStart.getPropertyModelPositionX().setValueAndUnit(10, LengthProperty.Unit.MM);
    pointModelStart.getPropertyModelPositionY().setValueAndUnit(10, LengthProperty.Unit.MM);

    PointModel pointModelEnd = new PointModel();
    pointModelEnd.getPropertyModelPositionX().setValueAndUnit(50, LengthProperty.Unit.MM);
    pointModelEnd.getPropertyModelPositionY().setValueAndUnit(50, LengthProperty.Unit.MM);

    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathControlPoints().setText("0.2,-0.4;1,-0.4");
    pathModel.setConnectedComponents(pointModelStart, pointModelEnd);

    PolyPathLength function = new PolyPathLength(manager, new PathLengthMath());
    double calculatedLength = function.applyAsDouble(pathModel);

    assertThat(calculatedLength, is(80.0));
  }

  @Test
  void testApplyAsDoubleThreeControlPoints() {
    PointModel pointModelStart = new PointModel();
    pointModelStart.getPropertyModelPositionX().setValueAndUnit(10, LengthProperty.Unit.MM);
    pointModelStart.getPropertyModelPositionY().setValueAndUnit(10, LengthProperty.Unit.MM);

    PointModel pointModelEnd = new PointModel();
    pointModelEnd.getPropertyModelPositionX().setValueAndUnit(70, LengthProperty.Unit.MM);
    pointModelEnd.getPropertyModelPositionY().setValueAndUnit(50, LengthProperty.Unit.MM);

    PathModel pathModel = new PathModel();
    pathModel.getPropertyPathControlPoints().setText("0.2,-0.4;1,-0.4;1,-1");
    pathModel.setConnectedComponents(pointModelStart, pointModelEnd);

    PolyPathLength function = new PolyPathLength(manager, new PathLengthMath());
    double calculatedLength = function.applyAsDouble(pathModel);

    assertThat(calculatedLength, is(100.0));
  }
}
