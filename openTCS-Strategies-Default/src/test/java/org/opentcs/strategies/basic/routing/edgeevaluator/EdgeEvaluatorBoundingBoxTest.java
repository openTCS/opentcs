/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.edgeevaluator.BoundingBoxProtrusionCheck.BoundingBoxProtrusion;

/**
 * Tests for {@link EdgeEvaluatorBoundingBox}.
 */
class EdgeEvaluatorBoundingBoxTest {

  private TCSObjectService objectService;
  private BoundingBoxProtrusionCheck protrusionCheck;
  private EdgeEvaluatorBoundingBox edgeEvaluator;

  @BeforeEach
  void setUp() {
    objectService = mock();
    protrusionCheck = mock();
    edgeEvaluator = new EdgeEvaluatorBoundingBox(objectService, protrusionCheck);
  }

  @Test
  void excludePathWhenVehicleBondingBoxProtrudesPointBoundingBox() {
    BoundingBox pointBoundingBox = new BoundingBox(1, 1, 1);
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(pointBoundingBox);
    Path path = new Path("1 -- 2", new Point("1").getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);

    BoundingBox vehicleBoundingBox = new BoundingBox(3, 3, 3);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(vehicleBoundingBox);

    when(objectService.fetchObject(Point.class, destPoint.getReference())).thenReturn(destPoint);
    when(protrusionCheck.checkProtrusion(vehicleBoundingBox, pointBoundingBox))
        .thenReturn(new BoundingBoxProtrusion(1, 1, 1, 1, 1));

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);
    verify(protrusionCheck).checkProtrusion(vehicleBoundingBox, pointBoundingBox);
  }

  @Test
  void includePathWhenVehicleBondingBoxDoesNotProtrudePointBoundingBox() {
    BoundingBox pointBoundingBox = new BoundingBox(3, 3, 3);
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(pointBoundingBox);
    Path path = new Path("1 -- 2", new Point("1").getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);

    BoundingBox vehicleBoundingBox = new BoundingBox(1, 1, 1);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(vehicleBoundingBox);

    when(objectService.fetchObject(Point.class, destPoint.getReference())).thenReturn(destPoint);
    when(protrusionCheck.checkProtrusion(vehicleBoundingBox, pointBoundingBox))
        .thenReturn(new BoundingBoxProtrusion(0, 0, 0, 0, 0));

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isEqualTo(0);
    verify(protrusionCheck).checkProtrusion(vehicleBoundingBox, pointBoundingBox);
  }
}
