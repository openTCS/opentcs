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
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

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
    protrusionCheck = new BoundingBoxProtrusionCheck();
    edgeEvaluator = new EdgeEvaluatorBoundingBox(objectService, protrusionCheck);
  }

  @Test
  void excludeForwardEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(1, 1, 1));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "2")).thenReturn(destPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);
  }

  @Test
  void includeForwardEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "2")).thenReturn(destPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isZero();
  }

  @Test
  void ignoreBoundingBoxProtrusionAtSourceVertexWithForwardEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(1, 1, 1));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "2")).thenReturn(destPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isZero();
  }

  @Test
  void excludeReverseEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(1, 1, 1));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, true);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "1")).thenReturn(srcPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);
  }

  @Test
  void includeReverseEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, true);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "1")).thenReturn(srcPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isZero();
  }

  @Test
  void ignoreBoundingBoxProtrusionAtSourceVertexWithReverseEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(1, 1, 1));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, true);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    when(objectService.fetchObject(Point.class, "1")).thenReturn(srcPoint);

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isZero();
  }
}
