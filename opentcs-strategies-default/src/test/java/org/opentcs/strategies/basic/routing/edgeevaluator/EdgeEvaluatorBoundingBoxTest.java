// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.RoutingContext;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Tests for {@link EdgeEvaluatorBoundingBox}.
 */
class EdgeEvaluatorBoundingBoxTest {

  private BoundingBoxProtrusionCheck protrusionCheck;
  private EdgeEvaluatorBoundingBox edgeEvaluator;

  @BeforeEach
  void setUp() {
    protrusionCheck = new BoundingBoxProtrusionCheck();
    edgeEvaluator = new EdgeEvaluatorBoundingBox(protrusionCheck);
  }

  @Test
  void excludeForwardEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(1, 1, 1));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isNaN();
  }

  @Test
  void includeForwardEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, false);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

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
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

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
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isNaN();
  }

  @Test
  void includeReverseEdge() {
    Point srcPoint = new Point("1").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Point destPoint = new Point("2").withMaxVehicleBoundingBox(new BoundingBox(5, 5, 5));
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference());
    Edge edge = new Edge(path, true);
    Vehicle vehicle = new Vehicle("vehicle").withBoundingBox(new BoundingBox(3, 3, 3));
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

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
    edgeEvaluator.onRoutingContextUpdated(
        new RoutingContext(
            new PlantModel("")
                .withPoints(Set.of(srcPoint, destPoint))
                .withPaths(Set.of(path))
        )
    );

    double result = edgeEvaluator.computeWeight(edge, vehicle);

    assertThat(result).isZero();
  }
}
