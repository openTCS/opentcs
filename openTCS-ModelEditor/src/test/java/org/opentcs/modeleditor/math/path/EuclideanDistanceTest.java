/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.math.path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;

/**
 * Unit tests for {@link EuclideanDistance}.
 */
public class EuclideanDistanceTest {

  private PointModel startPoint;
  private PointModel endPoint;
  private PathModel pathModel;
  private EuclideanDistance euclideanDistance;

  @BeforeEach
  public void setUp() {
    startPoint = new PointModel();
    endPoint = new PointModel();
    pathModel = new PathModel();
    pathModel.setConnectedComponents(startPoint, endPoint);

    euclideanDistance = new EuclideanDistance();
  }

  @Test
  public void returnZeroForSamePosition() {
    startPoint.getPropertyModelPositionX().setValueAndUnit(1000, LengthProperty.Unit.MM);
    startPoint.getPropertyModelPositionY().setValueAndUnit(1000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionX().setValueAndUnit(1000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionY().setValueAndUnit(1000, LengthProperty.Unit.MM);

    assertThat(euclideanDistance.applyAsDouble(pathModel), is(0.0));
  }

  @Test
  public void returnDistanceXForSameY() {
    startPoint.getPropertyModelPositionX().setValueAndUnit(2000, LengthProperty.Unit.MM);
    startPoint.getPropertyModelPositionY().setValueAndUnit(1000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionX().setValueAndUnit(4500, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionY().setValueAndUnit(1000, LengthProperty.Unit.MM);

    assertThat(euclideanDistance.applyAsDouble(pathModel), is(2500.0));
  }

  @Test
  public void returnDistanceYForSameX() {
    startPoint.getPropertyModelPositionX().setValueAndUnit(1000, LengthProperty.Unit.MM);
    startPoint.getPropertyModelPositionY().setValueAndUnit(3000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionX().setValueAndUnit(1000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionY().setValueAndUnit(7654, LengthProperty.Unit.MM);

    assertThat(euclideanDistance.applyAsDouble(pathModel), is(4654.0));
  }

  @Test
  public void returnEuclideanDistance() {
    startPoint.getPropertyModelPositionX().setValueAndUnit(2000, LengthProperty.Unit.MM);
    startPoint.getPropertyModelPositionY().setValueAndUnit(1000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionX().setValueAndUnit(4000, LengthProperty.Unit.MM);
    endPoint.getPropertyModelPositionY().setValueAndUnit(5000, LengthProperty.Unit.MM);

    assertThat(euclideanDistance.applyAsDouble(pathModel), is(closeTo(4472.0, 0.2)));
  }
}
